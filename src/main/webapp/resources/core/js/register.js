document.addEventListener('DOMContentLoaded', function () {
  const passwordInput = document.getElementById('password');
  const strengthFill = document.getElementById('strengthFill');
  const strengthText = document.getElementById('strengthText');

  if (!passwordInput || !strengthFill || !strengthText) return;

  function evaluarPassword(value) {
    const hasSpecialChar = /[!@#$%^&*(),.?":{}|<>]/.test(value);
    strengthFill.classList.remove('strength-low', 'strength-medium', 'strength-high');

    if (value.length === 0 || value.length <= 8) {
      strengthFill.classList.add('strength-low');
      strengthFill.style.width = value.length === 0 ? '0%' : '33%';
      strengthText.textContent = value.length === 0 ? 'Sin contrasena' : 'Contrasena debil';
    } else if (!hasSpecialChar) {
      strengthFill.classList.add('strength-medium');
      strengthFill.style.width = '66%';
      strengthText.textContent = 'Contrasena media';
    } else {
      strengthFill.classList.add('strength-high');
      strengthFill.style.width = '100%';
      strengthText.textContent = 'Contrasena fuerte';
    }
  }

  evaluarPassword(passwordInput.value);

  passwordInput.addEventListener('input', function () {
    evaluarPassword(passwordInput.value);
  });
});
