// Minimal REST helpers and UI wiring for DevCollab
const API = {
  get: (url) => fetch(url, { headers: { 'Accept': 'application/json' } }).then(r => r.json()),
  post: (url, data) => fetch(url, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json', 'Accept': 'application/json' },
    body: JSON.stringify(data)
  }).then(r => r.json()),
  patch: (url, data) => fetch(url, {
    method: 'PATCH',
    headers: { 'Content-Type': 'application/json', 'Accept': 'application/json' },
    body: JSON.stringify(data)
  }).then(r => r.json()),
  del: (url) => fetch(url, { method: 'DELETE' })
};

const fmtSeconds = (s) => {
  const h = Math.floor(s / 3600);
  const m = Math.floor((s % 3600) / 60);
  const sec = Math.floor(s % 60);
  return `${h.toString().padStart(2,'0')}:${m.toString().padStart(2,'0')}:${sec.toString().padStart(2,'0')}`;
};

// Projects page logic
window.DevcollabProjects = (() => {
  const base = '/api/projects';
  const els = {};

  async function load() {
    els.tableBody.innerHTML = '<tr><td colspan="7">Loading...</td></tr>';
    const items = await API.get(base);
    render(items);
  }

  function render(items) {
    els.tableBody.innerHTML = '';
    items.forEach(p => {
      const tr = document.createElement('tr');
      tr.innerHTML = `
        <td>${p.id}</td>
        <td><input data-id="${p.id}" class="inline-name" value="${p.name || ''}"></td>
        <td>${p.status}</td>
        <td>${fmtSeconds(p.totalSecondsSpent || 0)}</td>
        <td>${fmtSeconds(p.combinedSecondsSpent || 0)}</td>
        <td>${p.completedPomodoros || 0}/${p.estimatedPomodoros || 0}</td>
        <td>
          <button data-action="start" data-id="${p.id}">Start</button>
          <button data-action="stop" data-id="${p.id}">Stop</button>
          <select data-action="status" data-id="${p.id}">
            <option ${p.status==='TODO'?'selected':''}>TODO</option>
            <option ${p.status==='DOING'?'selected':''}>DOING</option>
            <option ${p.status==='DONE'?'selected':''}>DONE</option>
          </select>
          <button data-action="delete" data-id="${p.id}">Delete</button>
        </td>`;
      els.tableBody.appendChild(tr);
    });
  }

  async function onSubmit(e) {
    e.preventDefault();
    const data = {
      name: els.name.value.trim(),
      description: els.description.value.trim(),
      estimatedPomodoros: parseInt(els.estimated.value || '1', 10)
    };
    if (!data.name) return;
    await API.post(base, data);
    e.target.reset();
    load();
  }

  async function onTableClick(e) {
    const id = e.target.getAttribute('data-id');
    const action = e.target.getAttribute('data-action');
    if (!id || !action) return;
    if (action === 'start') {
      await API.post(`${base}/${id}/pomodoro/start`, {});
    } else if (action === 'stop') {
      await API.post(`${base}/${id}/pomodoro/stop`, {});
    } else if (action === 'delete') {
      await API.del(`${base}/${id}`);
    }
    load();
  }

  async function onTableChange(e) {
    const id = e.target.getAttribute('data-id');
    if (!id) return;
    if (e.target.getAttribute('data-action') === 'status') {
      const status = e.target.value;
      await API.patch(`${base}/${id}`, { status });
      load();
    }
  }

  let nameDebounce;
  async function onNameInput(e) {
    const id = e.target.getAttribute('data-id');
    const name = e.target.value;
    clearTimeout(nameDebounce);
    nameDebounce = setTimeout(async () => {
      await API.patch(`${base}/${id}`, { name });
      load();
    }, 500);
  }

  function init() {
    els.form = document.getElementById('project-form');
    els.name = document.getElementById('project-name');
    els.description = document.getElementById('project-description');
    els.estimated = document.getElementById('project-estimated');
    els.tableBody = document.getElementById('projects-tbody');
    els.table = document.getElementById('projects-table');
    els.form.addEventListener('submit', onSubmit);
    els.table.addEventListener('click', onTableClick);
    els.table.addEventListener('change', onTableChange);
    els.table.addEventListener('input', (ev) => {
      if (ev.target.classList.contains('inline-name')) onNameInput(ev);
    });
    load();
  }

  return { init };
})();

document.addEventListener('DOMContentLoaded', () => {
  const hook = document.getElementById('projects-page-hook');
  if (hook && window.DevcollabProjects) window.DevcollabProjects.init();
});
