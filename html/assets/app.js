// Marca o link ativo no menu principal, gerencia estado de auth (mock)
(function() {
  const path = location.pathname.split('/').pop() || 'index.html';
  const links = document.querySelectorAll('.main-nav a');
  links.forEach(a => {
    const href = a.getAttribute('href');
    if ((path === '' && href.endsWith('index.html')) || href.endsWith(path)) {
      a.setAttribute('aria-current', 'page');
    }
  });

  // Atualiza ano no rodapé
  const elAno = document.getElementById('ano');
  if (elAno) elAno.textContent = new Date().getFullYear();

  // ===== Auth de teste (somente front) =====
  const USER_KEY = 'care:user';

  function getUser() {
    try { return JSON.parse(localStorage.getItem(USER_KEY) || 'null'); } catch { return null; }
  }
  function setUser(user) {
    localStorage.setItem(USER_KEY, JSON.stringify(user));
    renderUserNav();
    updateBindings();
    updateNavVisibility();
  }
  function clearUser() {
    localStorage.removeItem(USER_KEY);
    renderUserNav();
    updateBindings();
    updateNavVisibility();
  }
  function initialsFromName(name = '') {
    const parts = name.trim().split(/\s+/).slice(0,2);
    return parts.map(p => p[0]?.toUpperCase() || '').join('') || 'U';
  }
  function nameFromEmail(email='') {
    const id = (email.split('@')[0] || '').replace(/[^a-zA-Z0-9._-]/g,'');
    return id.split(/[._-]+/).filter(Boolean).map(s => s.charAt(0).toUpperCase() + s.slice(1)).join(' ');
  }

  function renderUserNav() {
    const nav = document.querySelector('.user-nav');
    if (!nav) return;
    const user = getUser();
    if (user) {
      nav.innerHTML = `
        <span class="user-chip" title="${user.email}">
          <span class="avatar">${initialsFromName(user.name)}</span>
          <span class="greet">Olá, ${user.name.split(' ')[0]}</span>
        </span>
        <a class="btn btn-ghost" href="#" id="btnLogout">Sair</a>
      `;
      nav.querySelector('#btnLogout')?.addEventListener('click', (e)=>{ e.preventDefault(); clearUser(); location.href = 'index.html'; });
    } else {
      nav.innerHTML = `
        <a class="btn btn-ghost" href="login.html">Log In</a>
        <a class="btn btn-primary" href="login.html#cadastro">Cadastrar-se</a>
      `;
    }
  }

  renderUserNav();
  updateBindings();
  updateNavVisibility();

  function updateBindings() {
    const u = getUser();
    document.querySelectorAll('[data-bind="user-name"]').forEach(el => el.textContent = u?.name || '—');
    document.querySelectorAll('[data-bind="user-email"]').forEach(el => el.textContent = u?.email || '—');
    document.querySelectorAll('[data-bind="user-role"]').forEach(el => el.textContent = u?.role || '—');

    // visibilidade condicional
    document.querySelectorAll('[data-show-if]').forEach(el => {
      const cond = el.getAttribute('data-show-if');
      const logged = !!u;
      let show = false;
      if (cond === 'logged') show = logged;
      else if (cond === 'guest') show = !logged;
      else if (cond === 'empresa') show = logged && u.role === 'empresa';
      else if (cond === 'cliente') show = logged && u.role === 'cliente';
      el.hidden = !show;
    });
  }

  function updateNavVisibility() {
    const u = getUser();
    const body = document.body;
    body.classList.remove('auth-yes','role-empresa','role-cliente');
    if (u) {
      body.classList.add('auth-yes');
      body.classList.add(u.role === 'empresa' ? 'role-empresa' : 'role-cliente');
    }

    // Elementos com classes de requisito (fallback em JS)
    document.querySelectorAll('.requires-auth').forEach(el => {
      el.style.display = u ? '' : 'none';
    });
    document.querySelectorAll('.requires-role-empresa').forEach(el => {
      el.style.display = (u && u.role === 'empresa') ? '' : 'none';
    });
    document.querySelectorAll('.requires-guest').forEach(el => {
      el.style.display = u ? 'none' : '';
    });
  }

  // Bloqueio de páginas restritas e guarda de links
  const RESTRICTED = ['perfil.html','configuracoes.html','seguindo.html','minhas-pesquisas.html','criacao-de-pesquisa.html'];
  const isRestrictedPage = RESTRICTED.includes(path);
  const userNow = getUser();
  if (!userNow && isRestrictedPage) {
    const next = encodeURIComponent(path + (location.search || '') + (location.hash || ''));
    location.href = `login.html?next=${next}`;
  }

  // Página exclusiva para empresas
  if (path === 'criacao-de-pesquisa.html' && userNow && userNow.role !== 'empresa') {
    location.href = 'index.html';
  }

  document.querySelectorAll('a.requires-auth').forEach(a => {
    a.addEventListener('click', (e) => {
      if (!getUser()) {
        e.preventDefault();
        const href = a.getAttribute('href') || '';
        const next = encodeURIComponent(href);
        location.href = `login.html?next=${next}`;
      }
    });
  });

  // Tratamento de alternância Login/Cadastro dentro de login.html
  const formLogin = document.getElementById('formLogin');
  const formCadastro = document.getElementById('formCadastro');
  const abrirCadastro = document.getElementById('abrirCadastro');
  const linkCadastro = document.getElementById('linkCadastro');
  const linkLogin = document.getElementById('linkLogin');

  function mostrarCadastro() {
    if (!formLogin || !formCadastro) return;
    formLogin.classList.add('hidden');
    formCadastro.classList.remove('hidden');
    document.getElementById('tituloAuth')?.scrollIntoView({ behavior: 'smooth' });
    history.replaceState(null, '', '#cadastro');
  }

  function mostrarLogin() {
    if (!formLogin || !formCadastro) return;
    formCadastro.classList.add('hidden');
    formLogin.classList.remove('hidden');
    history.replaceState(null, '', '#login');
  }

  abrirCadastro?.addEventListener('click', (e) => { e.preventDefault(); mostrarCadastro(); });
  linkCadastro?.addEventListener('click', (e) => { e.preventDefault(); mostrarCadastro(); });
  linkLogin?.addEventListener('click', (e) => { e.preventDefault(); mostrarLogin(); });

  // Abre automaticamente a aba de cadastro se o hash indicar
  if (location.hash === '#cadastro') {
    mostrarCadastro();
  }

  // Submissão fake: salva usuário em localStorage e redireciona
  formLogin?.addEventListener('submit', (e) => {
    e.preventDefault();
    const data = new FormData(formLogin);
    const email = String(data.get('email') || '').trim();
    const name = nameFromEmail(email) || 'Usuário';
    if (!email) return alert('Informe um e-mail válido.');
    // mantém papel existente se já tiver cadastro
    const existing = getUser();
    const role = existing?.role || 'cliente';
    setUser({ name, email, role });
    const params = new URLSearchParams(location.search);
    const next = params.get('next');
    location.href = next ? decodeURIComponent(next) : 'index.html';
  });

  formCadastro?.addEventListener('submit', (e) => {
    e.preventDefault();
    const data = new FormData(formCadastro);
    const name = String(data.get('nome') || 'Usuário').trim();
    const email = String(data.get('email') || '').trim();
    const role = String(data.get('tipo') || 'cliente');
    if (!email) return alert('Informe um e-mail válido.');
    setUser({ name, email, role });
    const params = new URLSearchParams(location.search);
    const next = params.get('next');
    location.href = next ? decodeURIComponent(next) : 'index.html';
  });

  // ===== Tema (mock simples) =====
  const THEME_KEY = 'care:theme';
  function applyTheme(theme) {
    const root = document.documentElement;
    root.classList.toggle('theme-dark', theme === 'dark');
    localStorage.setItem(THEME_KEY, theme);
  }
  const savedTheme = localStorage.getItem(THEME_KEY) || 'light';
  applyTheme(savedTheme);
  document.querySelectorAll('[data-theme]')?.forEach(btn => {
    btn.addEventListener('click', () => applyTheme(btn.getAttribute('data-theme')));
  });
})();
