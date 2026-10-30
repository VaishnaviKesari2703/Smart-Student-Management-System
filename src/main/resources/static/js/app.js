/* Shared helpers: API client, layout, toasts, formatting. */
const BOOTSTRAP_CSS = 'https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css';

async function request(method, url, body) {
    const opts = { method, credentials: 'same-origin', headers: {} };
    if (body !== undefined) {
        opts.headers['Content-Type'] = 'application/json';
        opts.body = JSON.stringify(body);
    }
    const res = await fetch(url, opts);
    if (res.status === 401) {
        window.location.href = '/login.html';
        throw new Error('Session expired. Please sign in again.');
    }
    const text = await res.text();
    let data = null;
    if (text) {
        try { data = JSON.parse(text); } catch (e) { data = { message: text }; }
    }
    if (!res.ok) {
        const err = new Error((data && data.message) || ('Request failed (' + res.status + ')'));
        err.status = res.status;
        err.fieldErrors = data && data.fieldErrors;
        throw err;
    }
    return data;
}
const api = {
    get: (u) => request('GET', u),
    post: (u, b) => request('POST', u, b === undefined ? {} : b),
    put: (u, b) => request('PUT', u, b),
    del: (u) => request('DELETE', u)
};

function esc(v) {
    return String(v ?? '').replace(/[&<>"']/g, c => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[c]));
}

function toast(message, type) {
    let wrap = document.getElementById('toast-wrap');
    if (!wrap) {
        wrap = document.createElement('div');
        wrap.id = 'toast-wrap';
        wrap.className = 'toast-wrap';
        document.body.appendChild(wrap);
    }
    const el = document.createElement('div');
    el.className = 'toast-msg ' + (type || '');
    el.textContent = message;
    wrap.appendChild(el);
    setTimeout(() => el.remove(), 3600);
}

function statusBadge(att) {
    return att.eligible
        ? '<span class="badge-status eligible"><i class="bi bi-check-circle-fill"></i>Eligible</span>'
        : '<span class="badge-status shortage"><i class="bi bi-exclamation-triangle-fill"></i>Shortage</span>';
}

function progressBar(att) {
    return '<div class="progress progress-slim" role="progressbar" aria-valuenow="' + att.percentage + '" aria-valuemin="0" aria-valuemax="100">'
        + '<div class="progress-bar ' + (att.eligible ? 'bg-eligible' : 'bg-shortage') + '" style="width:' + att.percentage + '%"></div></div>';
}

function formatDateTime(iso) {
    const d = new Date(iso);
    return isNaN(d) ? esc(iso) : d.toLocaleString(undefined, { day: '2-digit', month: 'short', year: 'numeric', hour: '2-digit', minute: '2-digit', second: '2-digit' });
}

function actionLabel(action) {
    return { INCREMENT: '+1 (plus button)', DECREMENT: '-1 (minus button)', ADMIN_EDIT: 'Set by admin' }[action] || action;
}

/** Renders the sidebar into #sidebar and wires the mobile toggle. role: 'ADMIN' | 'STUDENT'. */
async function initLayout(role, activeKey) {
    const me = await api.get('/api/auth/me');
    if (me.role !== role) {
        window.location.href = '/';
        return null;
    }
    const adminNav = [
        ['dashboard', '/admin/dashboard.html', 'bi-speedometer2', 'Dashboard'],
        ['students', '/admin/students.html', 'bi-people', 'Students'],
        ['add', '/admin/student-form.html', 'bi-person-plus', 'Add Student'],
        ['attendance', '/admin/attendance.html', 'bi-clipboard-data', 'Attendance Monitoring']
    ];
    const studentNav = [['dashboard', '/student/dashboard.html', 'bi-house-door', 'My Dashboard']];
    const items = (role === 'ADMIN' ? adminNav : studentNav).map(n =>
        '<a class="nav-item ' + (n[0] === activeKey ? 'active' : '') + '" href="' + n[1] + '"><i class="bi ' + n[2] + '"></i>' + n[3] + '</a>').join('');
    const sidebar = document.getElementById('sidebar');
    sidebar.className = 'sidebar';
    sidebar.innerHTML =
        '<div class="brand"><i class="bi bi-mortarboard-fill"></i><span>Smart Student<br><small style="font-weight:500;color:#9fb0d3">Management System</small></span></div>'
        + '<div class="nav-label">' + (role === 'ADMIN' ? 'Administration' : 'Student') + '</div>' + items
        + '<div class="sidebar-user"><small>Signed in as</small><strong>' + esc(me.displayName) + '</strong>'
        + '<small>' + esc(me.role) + ' &middot; ' + esc(me.username) + '</small>'
        + '<a class="btn btn-sm btn-outline-light w-100 mt-2" id="logout-btn" href="/logout"><i class="bi bi-box-arrow-right me-1"></i>Logout</a></div>';
    const toggle = document.getElementById('menu-toggle');
    if (toggle) toggle.addEventListener('click', () => sidebar.classList.toggle('open'));
    sidebar.addEventListener('click', e => { if (e.target.closest('a.nav-item')) sidebar.classList.remove('open'); });
    document.addEventListener('click', e => {
        if (sidebar.classList.contains('open') && !sidebar.contains(e.target) && !e.target.closest('#menu-toggle')) sidebar.classList.remove('open');
    });
    return me;
}

function debounce(fn, ms) {
    let t;
    return (...a) => { clearTimeout(t); t = setTimeout(() => fn(...a), ms); };
}

/** Calls fn now and then every `ms` while the tab is visible (keeps admin pages in sync). */
function autoRefresh(fn, ms) {
    fn();
    setInterval(() => { if (!document.hidden) fn().catch(() => {}); }, ms);
}
