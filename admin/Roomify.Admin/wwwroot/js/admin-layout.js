document.addEventListener(
    "DOMContentLoaded",
    function () {

        const sidebar =
            document.getElementById(
                "adminSidebar"
            );

        const toggle =
            document.getElementById(
                "sidebarToggle"
            );

        const overlay =
            document.getElementById(
                "sidebarOverlay"
            );


        function openSidebar() {

            if (!sidebar || !overlay) {
                return;
            }

            sidebar.classList.add("open");

            overlay.classList.add("show");
        }


        function closeSidebar() {

            if (!sidebar || !overlay) {
                return;
            }

            sidebar.classList.remove("open");

            overlay.classList.remove("show");
        }


        if (toggle) {

            toggle.addEventListener(
                "click",
                function () {

                    if (
                        sidebar &&
                        sidebar.classList.contains(
                            "open"
                        )
                    ) {
                        closeSidebar();
                    }
                    else {
                        openSidebar();
                    }

                });
        }


        if (overlay) {

            overlay.addEventListener(
                "click",
                closeSidebar);
        }


        window.addEventListener(
            "resize",
            function () {

                if (window.innerWidth >= 992) {
                    closeSidebar();
                }

            });

    });