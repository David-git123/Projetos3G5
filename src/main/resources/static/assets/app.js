// Marca o link ativo no menu principal, gerencia estado de auth conectado ao backend
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

  // ===== Auth conectado ao backend =====
  const USER_KEY = 'care:user';
  const API_BASE = '/api/auth';

  // Função para fazer requisições ao backend
  async function apiRequest(url, options = {}) {
    try {
      const fullUrl = API_BASE + url;
      console.log('[DEBUG] Fazendo requisição:', fullUrl, options.method || 'GET');
      const response = await fetch(fullUrl, {
        ...options,
        headers: {
          'Content-Type': 'application/json',
          ...options.headers
        },
        credentials: 'include' // Importante para manter a sessão
      });
      const data = await response.json();
      console.log('[DEBUG] Resposta recebida:', data);
      return data;
    } catch (error) {
      console.error('[DEBUG] Erro na requisição:', error);
      return { success: false, message: 'Erro ao conectar com o servidor: ' + error.message };
    }
  }

  // Buscar usuário atual do backend
  async function fetchCurrentUser() {
    try {
      console.log('[DEBUG] Buscando usuário atual do backend...');
      const response = await apiRequest('/user');
      console.log('[DEBUG] Resposta do backend:', response);
      if (response.success && response.user) {
        console.log('[DEBUG] Usuário encontrado:', response.user);
        setUser(response.user);
        return response.user;
      } else {
        console.log('[DEBUG] Nenhum usuário autenticado');
        clearUser();
      }
      return null;
    } catch (error) {
      console.error('[DEBUG] Erro ao buscar usuário:', error);
      return null;
    }
  }

  function getUser() {
    try { 
      return JSON.parse(localStorage.getItem(USER_KEY) || 'null'); 
    } catch { 
      return null; 
    }
  }

  function setUser(user) {
    if (user) {
      localStorage.setItem(USER_KEY, JSON.stringify(user));
    } else {
      localStorage.removeItem(USER_KEY);
    }
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
      nav.querySelector('#btnLogout')?.addEventListener('click', async (e) => {
        e.preventDefault();
        await apiRequest('/logout', { method: 'POST' });
        clearUser();
        location.href = 'index.html';
      });
    } else {
      nav.innerHTML = `
        <a class="btn btn-ghost" href="login.html">Log In</a>
        <a class="btn btn-primary" href="login.html#cadastro">Cadastrar-se</a>
      `;
    }
  }

  // Carregar usuário ao iniciar
  fetchCurrentUser().then(() => {
    renderUserNav();
    updateBindings();
    updateNavVisibility();
  });

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
      else if (cond === 'empresa') show = logged && (u.role === 'empresa' || u.role === 'administrador');
      else if (cond === 'cliente') show = logged && (u.role === 'cliente');
      el.hidden = !show;
    });
  }

  function updateNavVisibility() {
    const u = getUser();
    const body = document.body;
    body.classList.remove('auth-yes','role-empresa','role-cliente');
    if (u) {
      body.classList.add('auth-yes');
      const role = u.role === 'administrador' ? 'empresa' : u.role;
      body.classList.add(role === 'empresa' ? 'role-empresa' : 'role-cliente');
    }

    // Elementos com classes de requisito (fallback em JS)
    document.querySelectorAll('.requires-auth').forEach(el => {
      el.style.display = u ? '' : 'none';
    });
    document.querySelectorAll('.requires-role-empresa').forEach(el => {
      el.style.display = (u && (u.role === 'empresa' || u.role === 'administrador')) ? '' : 'none';
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
  if (path === 'criacao-de-pesquisa.html' && userNow && userNow.role !== 'empresa' && userNow.role !== 'administrador') {
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

  // Submissão de login conectado ao backend
  formLogin?.addEventListener('submit', async (e) => {
    e.preventDefault();
    const data = new FormData(formLogin);
    const email = String(data.get('email') || '').trim();
    const password = String(data.get('password') || '').trim();
    
    if (!email || !password) {
      alert('Por favor, preencha todos os campos.');
      return;
    }

    const button = formLogin.querySelector('button[type="submit"]');
    const originalText = button.textContent;
    button.disabled = true;
    button.textContent = 'Entrando...';

    try {
      const response = await apiRequest('/login', {
        method: 'POST',
        body: JSON.stringify({ email, password })
      });

      if (response.success && response.user) {
        setUser(response.user);
        const params = new URLSearchParams(location.search);
        const next = params.get('next');
        location.href = next ? decodeURIComponent(next) : 'index.html';
      } else {
        alert(response.message || 'Email ou senha incorretos');
      }
    } catch (error) {
      alert('Erro ao fazer login. Tente novamente.');
      console.error(error);
    } finally {
      button.disabled = false;
      button.textContent = originalText;
    }
  });

  // Submissão de cadastro conectado ao backend
  formCadastro?.addEventListener('submit', async (e) => {
    e.preventDefault();
    const data = new FormData(formCadastro);
    const nome = String(data.get('nome') || '').trim();
    const email = String(data.get('email') || '').trim();
    const password = String(data.get('password') || '').trim();
    const tipo = String(data.get('tipo') || 'cliente');
    
    if (!nome || !email || !password) {
      alert('Por favor, preencha todos os campos.');
      return;
    }

    const button = formCadastro.querySelector('button[type="submit"]');
    const originalText = button.textContent;
    button.disabled = true;
    button.textContent = 'Criando conta...';

    try {
      const response = await apiRequest('/register', {
        method: 'POST',
        body: JSON.stringify({ nome, email, password, tipo })
      });

      if (response.success) {
        alert('Cadastro realizado com sucesso! Faça login para continuar.');
        mostrarLogin();
        formLogin.querySelector('input[name="email"]').value = email;
      } else {
        alert(response.message || 'Erro ao cadastrar. Tente novamente.');
      }
    } catch (error) {
      alert('Erro ao cadastrar. Tente novamente.');
      console.error(error);
    } finally {
      button.disabled = false;
      button.textContent = originalText;
    }
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

