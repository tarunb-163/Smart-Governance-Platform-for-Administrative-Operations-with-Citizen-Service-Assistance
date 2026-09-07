/* =========================================================
   CIVICPULSE - OFFICER COMPLAINTS
   complaints.js

   NOTE:
   This file does NOT create fake complaint data.
   Until the Spring Boot API is connected, all counts remain 0
   and the complaint table remains empty.
========================================================= */


/* =========================================================
   GLOBAL STATE
========================================================= */

const complaintsState = {
    /* =========================================================
   Testing Dommy Complaints
========================================================= */
    complaints: [
        {
            id: "CP-2026-001",
            title: "Water supply interruption",
            description: "Residents are facing interruption in regular water supply.",
            category: "WATER",
            location: "Ward 01, Davangere",
            priority: "HIGH",
            status: "PENDING",
            department: "Water Supply Department",
            citizenName: "Test Citizen 01",
            citizenContact: "9000000001",
            citizenEmail: "citizen01@example.com",
            createdAt: "2026-08-01T09:30:00"
        },

        {
            id: "CP-2026-002",
            title: "Damaged road",
            description: "A damaged section of road is causing difficulty for residents and vehicles.",
            category: "ROADS",
            location: "Ward 02, Davangere",
            priority: "HIGH",
            status: "IN_PROGRESS",
            department: "Public Works Department",
            citizenName: "Test Citizen 02",
            citizenContact: "9000000002",
            citizenEmail: "citizen02@example.com",
            createdAt: "2026-08-02T10:15:00"
        },

        {
            id: "CP-2026-003",
            title: "Street light not working",
            description: "Street light is not functioning properly and the area remains dark at night.",
            category: "ELECTRICITY",
            location: "Ward 03, Davangere",
            priority: "MEDIUM",
            status: "RESOLVED",
            department: "Electricity Department",
            citizenName: "Test Citizen 03",
            citizenContact: "9000000003",
            citizenEmail: "citizen03@example.com",
            createdAt: "2026-08-03T11:00:00"
        },

        {
            id: "CP-2026-004",
            title: "Garbage collection issue",
            description: "Garbage has not been collected from the locality for several days.",
            category: "SANITATION",
            location: "Ward 04, Davangere",
            priority: "MEDIUM",
            status: "PENDING",
            department: "Sanitation Department",
            citizenName: "Test Citizen 04",
            citizenContact: "9000000004",
            citizenEmail: "citizen04@example.com",
            createdAt: "2026-08-04T08:45:00"
        },

        {
            id: "CP-2026-005",
            title: "Public health facility issue",
            description: "Residents have reported an issue with services at the local health facility.",
            category: "HEALTH",
            location: "Ward 05, Davangere",
            priority: "CRITICAL",
            status: "IN_PROGRESS",
            department: "Health Department",
            citizenName: "Test Citizen 05",
            citizenContact: "9000000005",
            citizenEmail: "citizen05@example.com",
            createdAt: "2026-08-05T12:20:00"
        },

        {
            id: "CP-2026-006",
            title: "Other civic issue",
            description: "General civic issue reported by the resident for departmental review.",
            category: "OTHER",
            location: "Ward 06, Davangere",
            priority: "LOW",
            status: "REJECTED",
            department: "General Administration",
            citizenName: "Test Citizen 06",
            citizenContact: "9000000006",
            citizenEmail: "citizen06@example.com",
            createdAt: "2026-08-06T14:10:00"
        }
    ],
    filteredComplaints: [],

    currentPage: 1,
    pageSize: 10,

    search: "",
    status: "",
    priority: "",
    category: "",

    loading: false
};


/* =========================================================
   DOM READY
========================================================= */

document.addEventListener("DOMContentLoaded", function () {

    initializeComplaintsPage();

});


/* =========================================================
   INITIALIZE PAGE
========================================================= */

function initializeComplaintsPage() {

    initializeFilters();

    initializeRefreshButtons();

    initializePagination();

    initializeSearch();

    loadComplaints();

}


/* =========================================================
   FILTER INITIALIZATION
========================================================= */

function initializeFilters() {

    const statusFilter =
        document.getElementById("statusFilter");

    const priorityFilter =
        document.getElementById("priorityFilter");

    const categoryFilter =
        document.getElementById("categoryFilter");


    if (statusFilter) {

        statusFilter.addEventListener("change", function () {

            complaintsState.status = this.value;

            complaintsState.currentPage = 1;

            markFilterActive(this);

            applyFilters();

        });

    }


    if (priorityFilter) {

        priorityFilter.addEventListener("change", function () {

            complaintsState.priority = this.value;

            complaintsState.currentPage = 1;

            markFilterActive(this);

            applyFilters();

        });

    }


    if (categoryFilter) {

        categoryFilter.addEventListener("change", function () {

            complaintsState.category = this.value;

            complaintsState.currentPage = 1;

            markFilterActive(this);

            applyFilters();

        });

    }


    const resetButton =
        document.getElementById("resetFilters");

    if (resetButton) {

        resetButton.addEventListener("click", function () {

            resetFilters();

        });

    }

}


/* =========================================================
   SEARCH
========================================================= */

function initializeSearch() {

    const searchInput =
        document.getElementById("complaintSearch");

    if (!searchInput) {
        return;
    }


    searchInput.addEventListener("input", function () {

        complaintsState.search =
            this.value.trim().toLowerCase();

        complaintsState.currentPage = 1;

        markFilterActive(this);

        applyFilters();

    });

}


/* =========================================================
   APPLY FILTERS
========================================================= */

function applyFilters() {

    let filtered =
        [...complaintsState.complaints];


    /* Search */

    if (complaintsState.search) {

        filtered = filtered.filter(function (complaint) {

            const complaintId =
                String(complaint.id || "")
                    .toLowerCase();

            const title =
                String(complaint.title || "")
                    .toLowerCase();

            const description =
                String(complaint.description || "")
                    .toLowerCase();

            const category =
                String(complaint.category || "")
                    .toLowerCase();

            const location =
                String(complaint.location || "")
                    .toLowerCase();


            return (
                complaintId.includes(complaintsState.search) ||
                title.includes(complaintsState.search) ||
                description.includes(complaintsState.search) ||
                category.includes(complaintsState.search) ||
                location.includes(complaintsState.search)
            );

        });

    }


    /* Status */

    if (complaintsState.status) {

        filtered = filtered.filter(function (complaint) {

            return normalizeValue(complaint.status) ===
                normalizeValue(complaintsState.status);

        });

    }


    /* Priority */

    if (complaintsState.priority) {

        filtered = filtered.filter(function (complaint) {

            return normalizeValue(complaint.priority) ===
                normalizeValue(complaintsState.priority);

        });

    }


    /* Category */

    if (complaintsState.category) {

        filtered = filtered.filter(function (complaint) {

            return normalizeValue(complaint.category) ===
                normalizeValue(complaintsState.category);

        });

    }


    complaintsState.filteredComplaints = filtered;

    renderComplaints();

}


/* =========================================================
   RENDER COMPLAINTS
========================================================= */

function renderComplaints() {

    const tableWrapper =
        document.getElementById("complaintsTableWrapper");

    const emptyState =
        document.getElementById("complaintsEmpty");

    const tableBody =
        document.getElementById("complaintsTableBody");

    const pagination =
        document.getElementById("complaintsPagination");


    if (!tableBody) {
        return;
    }


    tableBody.innerHTML = "";


    const complaints =
        complaintsState.filteredComplaints;


    /* No complaints */

    if (!complaints || complaints.length === 0) {

        if (tableWrapper) {
            tableWrapper.classList.add("d-none");
        }

        if (pagination) {
            pagination.classList.add("d-none");
        }

        if (emptyState) {
            emptyState.classList.remove("d-none");
        }

        updatePagination();

        return;

    }


    /* Show table */

    if (emptyState) {
        emptyState.classList.add("d-none");
    }

    if (tableWrapper) {
        tableWrapper.classList.remove("d-none");
    }


    const start =
        (complaintsState.currentPage - 1) *
        complaintsState.pageSize;

    const end =
        start +
        complaintsState.pageSize;


    const pageComplaints =
        complaints.slice(start, end);


    pageComplaints.forEach(function (complaint) {

        const row =
            createComplaintRow(complaint);

        tableBody.appendChild(row);

    });


    if (pagination) {
        pagination.classList.remove("d-none");
    }


    updatePagination();

}


/* =========================================================
   CREATE TABLE ROW
========================================================= */

function createComplaintRow(complaint) {

    const row =
        document.createElement("tr");


    const complaintId =
        escapeHtml(
            complaint.id || "—"
        );


    const category =
        escapeHtml(
            formatCategory(complaint.category)
        );


    const location =
        escapeHtml(
            complaint.location || "—"
        );


    const priority =
        normalizeValue(
            complaint.priority
        );


    const status =
        normalizeValue(
            complaint.status
        );


    const submittedDate =
        formatDate(
            complaint.createdAt ||
            complaint.submittedAt
        );


    row.innerHTML = `

        <td>

            <span class="complaint-id">

                ${complaintId}

            </span>

        </td>


        <td>

            <div class="complaint-category">

                <div class="complaint-category-icon">

                    <i class="${getCategoryIcon(
        complaint.category
    )}"></i>

                </div>

                <span class="complaint-category-name">

                    ${category}

                </span>

            </div>

        </td>


        <td>

            <div class="complaint-location">

                <i class="bi bi-geo-alt"></i>

                <span title="${location}">

                    ${location}

                </span>

            </div>

        </td>


        <td>

            ${createPriorityBadge(priority)}

        </td>


        <td>

            <span class="complaint-date">

                ${submittedDate}

            </span>

        </td>


        <td>

            ${createStatusBadge(status)}

        </td>


        <td class="text-end">

            <a
                href="/officer/complaint-details?id=${encodeURIComponent(
        complaint.id || ""
    )}"
                class="complaint-action-button"
                title="View complaint">

                <i class="bi bi-eye"></i>

            </a>

        </td>

    `;


    return row;

}


/* =========================================================
   STATUS BADGE
========================================================= */

function createStatusBadge(status) {

    if (!status) {

        return `
            <span class="status-badge">
                Unknown
            </span>
        `;

    }


    let cssClass = "status-pending";

    let text = "Pending";


    switch (status) {

        case "PENDING":

            cssClass = "status-pending";
            text = "Pending";

            break;


        case "IN_PROGRESS":

            cssClass = "status-progress";
            text = "In Progress";

            break;


        case "RESOLVED":

            cssClass = "status-resolved";
            text = "Resolved";

            break;


        case "REJECTED":

            cssClass = "status-rejected";
            text = "Rejected";

            break;


        default:

            text =
                formatStatus(status);

    }


    return `
        <span class="status-badge ${cssClass}">
            ${escapeHtml(text)}
        </span>
    `;

}


/* =========================================================
   PRIORITY BADGE
========================================================= */

function createPriorityBadge(priority) {

    if (!priority) {

        return `
            <span class="badge bg-light text-secondary">
                Unknown
            </span>
        `;

    }


    let cssClass = "priority-low";

    let text = "Low";


    switch (priority) {

        case "LOW":

            cssClass = "priority-low";
            text = "Low";

            break;


        case "MEDIUM":

            cssClass = "priority-medium";
            text = "Medium";

            break;


        case "HIGH":

            cssClass = "priority-high";
            text = "High";

            break;


        case "CRITICAL":

            cssClass = "priority-critical";
            text = "Critical";

            break;


        default:

            text =
                formatStatus(priority);

    }


    return `
        <span class="badge ${cssClass}">
            ${escapeHtml(text)}
        </span>
    `;

}


/* =========================================================
   CATEGORY ICON
========================================================= */

function getCategoryIcon(category) {

    const value =
        normalizeValue(category);


    switch (value) {

        case "WATER":

            return "bi bi-droplet-fill";


        case "ROADS":

            return "bi bi-signpost-2-fill";


        case "ELECTRICITY":

            return "bi bi-lightning-charge-fill";


        case "SANITATION":

            return "bi bi-trash3-fill";


        case "HEALTH":

            return "bi bi-heart-pulse-fill";


        default:

            return "bi bi-file-earmark-text-fill";

    }

}


/* =========================================================
   CATEGORY FORMATTER
========================================================= */

function formatCategory(category) {

    if (!category) {
        return "Other";
    }


    const value =
        normalizeValue(category);


    const categories = {

        WATER: "Water Supply",

        ROADS: "Roads",

        ELECTRICITY: "Electricity",

        SANITATION: "Sanitation",

        HEALTH: "Health",

        OTHER: "Other"

    };


    return categories[value] ||
        formatStatus(value);

}


/* =========================================================
   STATUS FORMATTER
========================================================= */

function formatStatus(value) {

    if (!value) {
        return "Unknown";
    }


    return String(value)

        .toLowerCase()

        .split("_")

        .map(function (word) {

            return word.charAt(0).toUpperCase() +
                word.slice(1);

        })

        .join(" ");

}


/* =========================================================
   NORMALIZE VALUE
========================================================= */

function normalizeValue(value) {

    if (value === null ||
        value === undefined) {

        return "";

    }


    return String(value)
        .trim()
        .toUpperCase();

}


/* =========================================================
   DATE FORMATTER
========================================================= */

function formatDate(value) {

    if (!value) {
        return "—";
    }


    const date =
        new Date(value);


    if (Number.isNaN(date.getTime())) {

        return escapeHtml(
            String(value)
        );

    }


    return date.toLocaleDateString(
        "en-IN",
        {
            day: "2-digit",
            month: "short",
            year: "numeric"
        }
    );

}


/* =========================================================
   UPDATE SUMMARY COUNTS
========================================================= */

function updateComplaintSummary() {

    const complaints =
        complaintsState.complaints;


    const total =
        complaints.length;


    const pending =
        complaints.filter(function (complaint) {

            return normalizeValue(complaint.status) ===
                "PENDING";

        }).length;


    const inProgress =
        complaints.filter(function (complaint) {

            return normalizeValue(complaint.status) ===
                "IN_PROGRESS";

        }).length;


    const resolved =
        complaints.filter(function (complaint) {

            return normalizeValue(complaint.status) ===
                "RESOLVED";

        }).length;


    setElementText(
        "totalComplaints",
        total
    );

    setElementText(
        "pendingComplaints",
        pending
    );

    setElementText(
        "inProgressComplaints",
        inProgress
    );

    setElementText(
        "resolvedComplaints",
        resolved
    );

}


/* =========================================================
   PAGINATION INITIALIZATION
========================================================= */

function initializePagination() {

    const previous =
        document.getElementById("previousPage");

    const next =
        document.getElementById("nextPage");


    if (previous) {

        previous.addEventListener(
            "click",
            function () {

                if (
                    complaintsState.currentPage > 1
                ) {

                    complaintsState.currentPage--;

                    renderComplaints();

                }

            }
        );

    }


    if (next) {

        next.addEventListener(
            "click",
            function () {

                const totalPages =
                    Math.ceil(
                        complaintsState.filteredComplaints.length /
                        complaintsState.pageSize
                    );


                if (
                    complaintsState.currentPage <
                    totalPages
                ) {

                    complaintsState.currentPage++;

                    renderComplaints();

                }

            }
        );

    }

}


/* =========================================================
   UPDATE PAGINATION
========================================================= */

function updatePagination() {

    const total =
        complaintsState.filteredComplaints.length;


    const totalPages =
        Math.max(
            1,
            Math.ceil(
                total /
                complaintsState.pageSize
            )
        );


    const currentPage =
        complaintsState.currentPage;


    const start =
        total === 0
            ? 0
            : ((currentPage - 1) *
            complaintsState.pageSize) + 1;


    const end =
        total === 0
            ? 0
            : Math.min(
                currentPage *
                complaintsState.pageSize,
                total
            );


    setElementText(
        "paginationStart",
        start
    );

    setElementText(
        "paginationEnd",
        end
    );

    setElementText(
        "paginationTotal",
        total
    );

    setElementText(
        "currentPage",
        currentPage
    );


    const previous =
        document.getElementById("previousPage");

    const next =
        document.getElementById("nextPage");


    if (previous) {

        previous.disabled =
            currentPage <= 1;

    }


    if (next) {

        next.disabled =
            currentPage >= totalPages ||
            total === 0;

    }

}


/* =========================================================
   RESET FILTERS
========================================================= */

function resetFilters() {

    complaintsState.search = "";
    complaintsState.status = "";
    complaintsState.priority = "";
    complaintsState.category = "";

    complaintsState.currentPage = 1;


    const searchInput =
        document.getElementById("complaintSearch");

    const statusFilter =
        document.getElementById("statusFilter");

    const priorityFilter =
        document.getElementById("priorityFilter");

    const categoryFilter =
        document.getElementById("categoryFilter");


    if (searchInput) {
        searchInput.value = "";
        searchInput.classList.remove(
            "filter-active"
        );
    }

    if (statusFilter) {
        statusFilter.value = "";
        statusFilter.classList.remove(
            "filter-active"
        );
    }

    if (priorityFilter) {
        priorityFilter.value = "";
        priorityFilter.classList.remove(
            "filter-active"
        );
    }

    if (categoryFilter) {
        categoryFilter.value = "";
        categoryFilter.classList.remove(
            "filter-active"
        );
    }


    applyFilters();

}


/* =========================================================
   FILTER ACTIVE VISUAL
========================================================= */

function markFilterActive(element) {

    if (!element) {
        return;
    }


    if (element.value &&
        element.value.trim() !== "") {

        element.classList.add(
            "filter-active"
        );

    } else {

        element.classList.remove(
            "filter-active"
        );

    }

}


/* =========================================================
   REFRESH BUTTONS
========================================================= */

function initializeRefreshButtons() {

    const refreshButton =
        document.getElementById("refreshComplaints");

    const refreshTable =
        document.getElementById("refreshTable");


    if (refreshButton) {

        refreshButton.addEventListener(
            "click",
            function () {

                loadComplaints();

            }
        );

    }


    if (refreshTable) {

        refreshTable.addEventListener(
            "click",
            function () {

                loadComplaints();

            }
        );

    }

}


/* =========================================================
   LOAD COMPLAINTS
========================================================= */

async function loadComplaints() {

    if (complaintsState.loading) {
        return;
    }


    complaintsState.loading = true;


    showLoadingState();


    try {
        const response = await fetch('/api/officer/complaints');
        if (response.ok) {
            const data = await response.json();
            complaintsState.complaints = data;
            complaintsState.filteredComplaints = [...data];
        } else {
            complaintsState.complaints = [];
            complaintsState.filteredComplaints = [];
        }

        updateComplaintSummary();
        applyFilters();
    } catch (error) {

        console.error(
            "Unable to load complaints:",
            error
        );


        complaintsState.complaints = [];

        complaintsState.filteredComplaints = [];


        updateComplaintSummary();

        updateEmptyState();

    } finally {

        complaintsState.loading = false;

        hideLoadingState();

    }

}


/* =========================================================
   LOADING STATE
========================================================= */

function showLoadingState() {

    const loading =
        document.getElementById(
            "complaintsLoading"
        );

    const empty =
        document.getElementById(
            "complaintsEmpty"
        );

    const table =
        document.getElementById(
            "complaintsTableWrapper"
        );


    if (loading) {
        loading.classList.remove(
            "d-none"
        );
    }

    if (empty) {
        empty.classList.add(
            "d-none"
        );
    }

    if (table) {
        table.classList.add(
            "d-none"
        );
    }

}


/* =========================================================
   HIDE LOADING STATE
========================================================= */

function hideLoadingState() {

    const loading =
        document.getElementById(
            "complaintsLoading"
        );


    if (loading) {

        loading.classList.add(
            "d-none"
        );

    }

}


/* =========================================================
   EMPTY STATE
========================================================= */

function updateEmptyState() {

    const empty =
        document.getElementById(
            "complaintsEmpty"
        );

    const table =
        document.getElementById(
            "complaintsTableWrapper"
        );

    const pagination =
        document.getElementById(
            "complaintsPagination"
        );


    const hasComplaints =
        complaintsState.filteredComplaints.length >
        0;


    if (hasComplaints) {

        if (empty) {
            empty.classList.add(
                "d-none"
            );
        }

        if (table) {
            table.classList.remove(
                "d-none"
            );
        }

        if (pagination) {
            pagination.classList.remove(
                "d-none"
            );
        }

    } else {

        if (empty) {
            empty.classList.remove(
                "d-none"
            );
        }

        if (table) {
            table.classList.add(
                "d-none"
            );
        }

        if (pagination) {
            pagination.classList.add(
                "d-none"
            );
        }

    }

}


/* =========================================================
   UTILITY - SET ELEMENT TEXT
========================================================= */

function setElementText(
    elementId,
    value
) {

    const element =
        document.getElementById(
            elementId
        );


    if (element) {

        element.textContent =
            value;

    }

}


/* =========================================================
   HTML ESCAPE
========================================================= */

function escapeHtml(value) {

    if (
        value === null ||
        value === undefined
    ) {

        return "";

    }


    return String(value)

        .replace(
            /&/g,
            "&amp;"
        )

        .replace(
            /</g,
            "&lt;"
        )

        .replace(
            />/g,
            "&gt;"
        )

        .replace(
            /"/g,
            "&quot;"
        )

        .replace(
            /'/g,
            "&#039;"
        );

}