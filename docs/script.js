// Navbar scroll shadow
(function () {
  const navbar = document.querySelector('.navbar');
  if (!navbar) return;

  function onScroll() {
    if (window.scrollY > 10) {
      navbar.classList.add('scrolled');
    } else {
      navbar.classList.remove('scrolled');
    }
  }

  window.addEventListener('scroll', onScroll, { passive: true });
  onScroll();
})();

// Smooth scroll for anchor links
document.querySelectorAll('a[href^="#"]').forEach(function (anchor) {
  anchor.addEventListener('click', function (e) {
    const target = document.querySelector(this.getAttribute('href'));
    if (target) {
      e.preventDefault();
      target.scrollIntoView({ behavior: 'smooth', block: 'start' });
    }
  });
});

// Copy button for code blocks
(function () {
  const wrappers = document.querySelectorAll('.code-block-wrapper');

  wrappers.forEach(function (wrapper) {
    const pre = wrapper.querySelector('pre');
    if (!pre) return;

    const btn = document.createElement('button');
    btn.className = 'copy-btn';
    btn.textContent = '复制';
    btn.setAttribute('type', 'button');

    btn.addEventListener('click', function () {
      const code = pre.querySelector('code');
      const text = code ? code.textContent : pre.textContent;

      if (navigator.clipboard && navigator.clipboard.writeText) {
        navigator.clipboard.writeText(text).then(onSuccess).catch(fallback);
      } else {
        fallback();
      }

      function fallback() {
        const ta = document.createElement('textarea');
        ta.value = text;
        ta.style.position = 'fixed';
        ta.style.opacity = '0';
        document.body.appendChild(ta);
        ta.select();
        try { document.execCommand('copy'); onSuccess(); } catch (_) {}
        document.body.removeChild(ta);
      }

      function onSuccess() {
        btn.textContent = '已复制 ✓';
        btn.classList.add('copied');
        setTimeout(function () {
          btn.textContent = '复制';
          btn.classList.remove('copied');
        }, 2000);
      }
    });

    wrapper.appendChild(btn);
  });
})();
