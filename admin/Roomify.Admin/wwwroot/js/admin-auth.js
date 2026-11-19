document.addEventListener("DOMContentLoaded", function () {

    const passwordInput =
        document.getElementById("passwordInput");

    const togglePassword =
        document.getElementById("togglePassword");

    if (passwordInput && togglePassword) {

        togglePassword.addEventListener(
            "click",
            function () {

                const isPassword =
                    passwordInput.type === "password";

                passwordInput.type =
                    isPassword
                        ? "text"
                        : "password";

                togglePassword.setAttribute(
                    "aria-label",
                    isPassword
                        ? "Ẩn mật khẩu"
                        : "Hiện mật khẩu");
            });
    }


    const form =
        document.getElementById("adminLoginForm");

    const button =
        document.getElementById("loginButton");

    const buttonText =
        document.getElementById("loginButtonText");

    if (form && button && buttonText) {

        form.addEventListener(
            "submit",
            function () {
                if (window.jQuery &&
                    $.validator &&
                    !$(form).valid()) {
                    return;
                }

                button.disabled = true;

                buttonText.textContent =
                    "Đang đăng nhập...";
            });
    }

});