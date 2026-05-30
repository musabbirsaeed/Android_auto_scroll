const navToggle = document.querySelector('.nav-toggle');
const navLinks = document.querySelector('#site-menu');

navToggle?.addEventListener('click', () => {
  const isOpen = navLinks.classList.toggle('is-open');
  navToggle.setAttribute('aria-expanded', String(isOpen));
});

navLinks?.addEventListener('click', (event) => {
  if (event.target instanceof HTMLAnchorElement) {
    navLinks.classList.remove('is-open');
    navToggle?.setAttribute('aria-expanded', 'false');
  }
});

const year = document.querySelector('#year');
if (year) {
  year.textContent = String(new Date().getFullYear());
}
