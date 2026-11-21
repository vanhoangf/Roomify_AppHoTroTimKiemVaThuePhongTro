(() => {

    "use strict";


    // ============================================================
    // CHECK CHART.JS
    // ============================================================

    if (typeof Chart === "undefined") {
        console.error(
            "Chart.js chưa được tải."
        );

        return;
    }


    // ============================================================
    // READ JSON DATA
    // ============================================================

    function readJson(id) {

        const element =
            document.getElementById(id);


        if (!element) {
            return [];
        }


        try {

            const value =
                JSON.parse(
                    element.textContent
                );


            return Array.isArray(value)
                ? value
                : [];

        }
        catch (error) {

            console.error(
                `Không thể đọc dữ liệu chart: ${id}`,
                error
            );


            return [];
        }
    }


    const revenueData =
        readJson(
            "statisticsRevenueData"
        );


    const contractsData =
        readJson(
            "statisticsContractsData"
        );


    const usersData =
        readJson(
            "statisticsUsersData"
        );


    const roomsData =
        readJson(
            "statisticsRoomsData"
        );


    // ============================================================
    // FORMAT
    // ============================================================

    const numberFormatter =
        new Intl.NumberFormat(
            "vi-VN"
        );


    const currencyFormatter =
        new Intl.NumberFormat(
            "vi-VN",
            {
                maximumFractionDigits: 0
            }
        );


    function formatDate(value) {

        if (!value) {
            return "";
        }


        const parts =
            value
                .toString()
                .substring(0, 10)
                .split("-");


        if (parts.length !== 3) {
            return value;
        }


        return `${parts[2]}/${parts[1]}`;
    }


    function formatFullDate(value) {

        if (!value) {
            return "";
        }


        const parts =
            value
                .toString()
                .substring(0, 10)
                .split("-");


        if (parts.length !== 3) {
            return value;
        }


        return `${parts[2]}/${parts[1]}/${parts[0]}`;
    }


    // ============================================================
    // CHART DEFAULTS
    // ============================================================

    Chart.defaults.font.family =
        "Inter, system-ui, -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif";


    Chart.defaults.color =
        "#748197";


    Chart.defaults.animation.duration =
        450;


    function getMaxTicks(
        length
    ) {

        if (length <= 7) {
            return 7;
        }


        if (length <= 30) {
            return 8;
        }


        if (length <= 90) {
            return 9;
        }


        return 10;
    }


    function commonScales(
        dataLength,
        valueFormatter = value =>
            numberFormatter.format(value)
    ) {

        return {

            x: {

                border: {
                    display: false
                },

                grid: {
                    display: false
                },

                ticks: {

                    color:
                        "#8793a5",

                    font: {
                        size: 9
                    },

                    maxRotation: 0,

                    autoSkip: true,

                    maxTicksLimit:
                        getMaxTicks(
                            dataLength
                        )
                }
            },


            y: {

                beginAtZero: true,

                border: {
                    display: false
                },

                grid: {
                    color:
                        "#edf1f5",

                    drawTicks:
                        false
                },

                ticks: {

                    color:
                        "#8793a5",

                    padding:
                        10,

                    font: {
                        size: 9
                    },

                    callback:
                        valueFormatter
                }
            }

        };
    }


    function commonPlugins(
        tooltipFormatter
    ) {

        return {

            legend: {
                display: false
            },


            tooltip: {

                displayColors:
                    false,

                padding:
                    10,

                titleFont: {
                    size: 10,
                    weight: "600"
                },

                bodyFont: {
                    size: 10
                },

                callbacks: {

                    title(items) {

                        if (!items.length) {
                            return "";
                        }


                        const rawDate =
                            items[0]
                                .chart
                                .$rawDates?.[
                            items[0].dataIndex
                            ];


                        return formatFullDate(
                            rawDate
                        );
                    },


                    label(context) {

                        return tooltipFormatter(
                            context.parsed.y
                        );
                    }
                }
            }

        };
    }


    // ============================================================
    // REVENUE CHART
    // ============================================================

    function createRevenueChart() {

        const canvas =
            document.getElementById(
                "statisticsRevenueChart"
            );


        if (!canvas) {
            return;
        }


        const labels =
            revenueData.map(
                item =>
                    formatDate(
                        item.date
                        ?? item.Date
                    )
            );


        const values =
            revenueData.map(
                item =>
                    Number(
                        item.amount
                        ?? item.Amount
                        ?? 0
                    )
            );


        const rawDates =
            revenueData.map(
                item =>
                    item.date
                    ?? item.Date
            );


        const chart =
            new Chart(
                canvas,
                {

                    type: "line",


                    data: {

                        labels,

                        datasets: [
                            {
                                data: values,

                                borderColor:
                                    "#15945c",

                                backgroundColor:
                                    "rgba(21, 148, 92, 0.09)",

                                borderWidth:
                                    2,

                                fill:
                                    true,

                                tension:
                                    0.32,

                                pointRadius:
                                    values.length <= 30
                                        ? 2.5
                                        : 0,

                                pointHoverRadius:
                                    4,

                                pointBackgroundColor:
                                    "#15945c",

                                pointBorderWidth:
                                    0
                            }
                        ]
                    },


                    options: {

                        responsive:
                            true,

                        maintainAspectRatio:
                            false,

                        interaction: {
                            intersect: false,
                            mode: "index"
                        },


                        plugins:
                            commonPlugins(
                                value =>
                                    `${currencyFormatter.format(value)} đ`
                            ),


                        scales:
                            commonScales(
                                values.length,
                                value => {

                                    const number =
                                        Number(value);


                                    if (number >= 1_000_000) {

                                        return `${(
                                            number
                                            / 1_000_000
                                        ).toLocaleString(
                                            "vi-VN",
                                            {
                                                maximumFractionDigits: 1
                                            }
                                        )} tr`;
                                    }


                                    if (number >= 1_000) {

                                        return `${(
                                            number
                                            / 1_000
                                        ).toLocaleString(
                                            "vi-VN",
                                            {
                                                maximumFractionDigits: 0
                                            }
                                        )}k`;
                                    }


                                    return numberFormatter
                                        .format(number);
                                }
                            )
                    }
                }
            );


        chart.$rawDates =
            rawDates;
    }


    // ============================================================
    // GENERIC COUNT CHART
    // ============================================================

    function createCountChart({
        canvasId,
        data,
        borderColor,
        backgroundColor,
        label
    }) {

        const canvas =
            document.getElementById(
                canvasId
            );


        if (!canvas) {
            return;
        }


        const labels =
            data.map(
                item =>
                    formatDate(
                        item.date
                        ?? item.Date
                    )
            );


        const values =
            data.map(
                item =>
                    Number(
                        item.count
                        ?? item.Count
                        ?? 0
                    )
            );


        const rawDates =
            data.map(
                item =>
                    item.date
                    ?? item.Date
            );


        const chart =
            new Chart(
                canvas,
                {

                    type: "bar",


                    data: {

                        labels,

                        datasets: [
                            {
                                data:
                                    values,

                                backgroundColor,

                                borderColor,

                                borderWidth:
                                    1,

                                borderRadius:
                                    4,

                                borderSkipped:
                                    false,

                                maxBarThickness:
                                    34
                            }
                        ]
                    },


                    options: {

                        responsive:
                            true,

                        maintainAspectRatio:
                            false,

                        interaction: {
                            intersect: false,
                            mode: "index"
                        },


                        plugins:
                            commonPlugins(
                                value =>
                                    `${label}: ${numberFormatter.format(value)}`
                            ),


                        scales:
                            commonScales(
                                values.length,
                                value =>
                                    numberFormatter
                                        .format(value)
                            )
                    }
                }
            );


        chart.$rawDates =
            rawDates;
    }


    // ============================================================
    // CREATE
    // ============================================================

    createRevenueChart();


    createCountChart({

        canvasId:
            "statisticsContractsChart",

        data:
            contractsData,

        borderColor:
            "#1764df",

        backgroundColor:
            "rgba(23, 100, 223, 0.17)",

        label:
            "Hợp đồng"
    });


    createCountChart({

        canvasId:
            "statisticsUsersChart",

        data:
            usersData,

        borderColor:
            "#6656d8",

        backgroundColor:
            "rgba(102, 86, 216, 0.16)",

        label:
            "Người dùng"
    });


    createCountChart({

        canvasId:
            "statisticsRoomsChart",

        data:
            roomsData,

        borderColor:
            "#16889a",

        backgroundColor:
            "rgba(22, 136, 154, 0.16)",

        label:
            "Phòng"
    });

})();