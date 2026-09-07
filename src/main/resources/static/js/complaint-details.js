/* =========================================================
   CIVICPULSE - COMPLAINT DETAILS
   complaint-details.js

   TEMPORARY FRONTEND TEST DATA

   This will later be replaced by:
   GET /api/officer/complaints/{id}
========================================================= */


/* =========================================================
   TEST COMPLAINT DATA
========================================================= */

const testComplaints = [

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

];


/* =========================================================
   CURRENT COMPLAINT
========================================================= */

let currentComplaint = null;


/* =========================================================
   PAGE LOAD
========================================================= */

document.addEventListener("DOMContentLoaded", function () {

    initializeComplaintDetails();

});


/* =========================================================
   INITIALIZE
========================================================= */

function initializeComplaintDetails() {

    const complaintId =
        getComplaintIdFromUrl();


    if (!complaintId) {

        showNotFound();

        return;

    }


    loadComplaint(complaintId);

}


/* =========================================================
   GET COMPLAINT ID FROM URL
========================================================= */

function getComplaintIdFromUrl() {

    const params =
        new URLSearchParams(
            window.location.search
        );


    return params.get("id");

}


/* =========================================================
   LOAD COMPLAINT
========================================================= */

function loadComplaint(complaintId) {

    showLoading();

    fetch('/api/officer/complaints/' + encodeURIComponent(complaintId))
        .then(response => {
            if (!response.ok) throw new Error("Not found");
            return response.json();
        })
        .then(complaint => {
            currentComplaint = complaint;
            populateComplaintDetails(complaint);
            showComplaintContent();
        })
        .catch(() => {
            const complaint = testComplaints.find(item => item.id === complaintId);
            if (!complaint) {
                showNotFound();
                return;
            }
            currentComplaint = complaint;
            populateComplaintDetails(complaint);
            showComplaintContent();
        });
}


/* =========================================================
   POPULATE COMPLAINT DETAILS
========================================================= */

function populateComplaintDetails(
    complaint
) {


    /* =====================================================
       BASIC INFORMATION
    ====================================================== */

    setText(
        "complaintId",
        complaint.id
    );


    setText(
        "complaintTitle",
        complaint.title
    );


    setText(
        "complaintDescription",
        complaint.description
    );


    setText(
        "complaintLocation",
        complaint.location
    );


    setText(
        "complaintCategory",
        formatCategory(
            complaint.category
        )
    );


    setText(
        "complaintDepartment",
        complaint.department
    );


    /* =====================================================
       CITIZEN INFORMATION
    ====================================================== */

    setText(
        "citizenName",
        complaint.citizenName
    );


    setText(
        "citizenContact",
        complaint.citizenContact
    );


    setText(
        "citizenEmail",
        complaint.citizenEmail
    );


    /* =====================================================
       DATE
    ====================================================== */

    const formattedDate =
        formatDate(
            complaint.createdAt
        );


    setText(
        "complaintSubmittedDate",
        "Submitted " + formattedDate
    );


    setText(
        "submittedTimelineDate",
        formattedDate
    );


    /* =====================================================
       STATUS
    ====================================================== */

    renderStatus(
        complaint.status
    );


    /* =====================================================
       PRIORITY
    ====================================================== */

    renderPriority(
        complaint.priority
    );


    /* =====================================================
       UPDATE BUTTON
    ====================================================== */

    const updateButton =
        document.getElementById(
            "updateComplaintButton"
        );


    if (updateButton) {

        updateButton.href =
            "/officer/update-complaint?id=" +
            encodeURIComponent(
                complaint.id
            );

    }


    /* =====================================================
       TIMELINE
    ====================================================== */

    renderTimeline(
        complaint
    );

}


/* =========================================================
   RENDER STATUS
========================================================= */

function renderStatus(status) {

    const container =
        document.getElementById(
            "complaintStatus"
        );


    if (!container) {
        return;
    }


    const normalized =
        normalizeValue(status);


    let cssClass =
        "status-pending";


    let text =
        "Pending";


    switch (normalized) {

        case "PENDING":

            cssClass =
                "status-pending";

            text =
                "Pending";

            break;


        case "IN_PROGRESS":

            cssClass =
                "status-progress";

            text =
                "In Progress";

            break;


        case "RESOLVED":

            cssClass =
                "status-resolved";

            text =
                "Resolved";

            break;


        case "REJECTED":

            cssClass =
                "status-rejected";

            text =
                "Rejected";

            break;


        default:

            text =
                formatStatus(
                    normalized
                );

    }


    container.innerHTML = `

        <span class="status-badge ${cssClass}">

            ${escapeHtml(text)}

        </span>

    `;

}


/* =========================================================
   RENDER PRIORITY
========================================================= */

function renderPriority(priority) {

    const container =
        document.getElementById(
            "complaintPriority"
        );


    if (!container) {
        return;
    }


    const normalized =
        normalizeValue(priority);


    let cssClass =
        "priority-low";


    let text =
        "Low";


    switch (normalized) {

        case "LOW":

            cssClass =
                "priority-low";

            text =
                "Low";

            break;


        case "MEDIUM":

            cssClass =
                "priority-medium";

            text =
                "Medium";

            break;


        case "HIGH":

            cssClass =
                "priority-high";

            text =
                "High";

            break;


        case "CRITICAL":

            cssClass =
                "priority-critical";

            text =
                "Critical";

            break;


        default:

            text =
                formatStatus(
                    normalized
                );

    }


    container.innerHTML = `

        <span class="badge ${cssClass}">

            ${escapeHtml(text)}

        </span>

    `;

}


/* =========================================================
   RENDER TIMELINE
========================================================= */

function renderTimeline(complaint) {

    const submittedDate =
        formatDate(
            complaint.createdAt
        );


    setText(
        "submittedTimelineDate",
        submittedDate
    );


    /*
     * The temporary test data does not contain
     * separate assignment / processing / resolution
     * timestamps.
     *
     * Therefore we don't invent dates.
     */


    setText(
        "assignedTimelineDate",
        "—"
    );


    setText(
        "progressTimelineDate",
        "—"
    );


    setText(
        "resolvedTimelineDate",
        "—"
    );


    updateTimelineState(
        complaint.status
    );

}


/* =========================================================
   UPDATE TIMELINE STATE
========================================================= */

function updateTimelineState(status) {

    const normalized =
        normalizeValue(status);


    const timelineItems =
        document.querySelectorAll(
            ".timeline-item"
        );


    if (!timelineItems.length) {
        return;
    }


    /*
     * Timeline order:
     *
     * 0 = Submitted
     * 1 = Assigned
     * 2 = Processing
     * 3 = Resolved
     */


    let activeIndex = 0;


    switch (normalized) {

        case "PENDING":

            activeIndex = 1;

            break;


        case "IN_PROGRESS":

            activeIndex = 2;

            break;


        case "RESOLVED":

            activeIndex = 3;

            break;


        case "REJECTED":

            activeIndex = 2;

            break;


        default:

            activeIndex = 0;

    }


    timelineItems.forEach(
        function (item, index) {

            item.classList.remove(
                "completed",
                "current",
                "inactive"
            );


            if (index < activeIndex) {

                item.classList.add(
                    "completed"
                );

            } else if (
                index === activeIndex
            ) {

                item.classList.add(
                    "current"
                );

            } else {

                item.classList.add(
                    "inactive"
                );

            }

        }
    );

}


/* =========================================================
   SHOW LOADING
========================================================= */

function showLoading() {

    const loading =
        document.getElementById(
            "complaintLoading"
        );


    const notFound =
        document.getElementById(
            "complaintNotFound"
        );


    const content =
        document.getElementById(
            "complaintDetailsContent"
        );


    if (loading) {

        loading.classList.remove(
            "d-none"
        );

    }


    if (notFound) {

        notFound.classList.add(
            "d-none"
        );

    }


    if (content) {

        content.classList.add(
            "d-none"
        );

    }

}


/* =========================================================
   SHOW COMPLAINT CONTENT
========================================================= */

function showComplaintContent() {

    const loading =
        document.getElementById(
            "complaintLoading"
        );


    const notFound =
        document.getElementById(
            "complaintNotFound"
        );


    const content =
        document.getElementById(
            "complaintDetailsContent"
        );


    if (loading) {

        loading.classList.add(
            "d-none"
        );

    }


    if (notFound) {

        notFound.classList.add(
            "d-none"
        );

    }


    if (content) {

        content.classList.remove(
            "d-none"
        );

    }

}


/* =========================================================
   SHOW NOT FOUND
========================================================= */

function showNotFound() {

    const loading =
        document.getElementById(
            "complaintLoading"
        );


    const notFound =
        document.getElementById(
            "complaintNotFound"
        );


    const content =
        document.getElementById(
            "complaintDetailsContent"
        );


    if (loading) {

        loading.classList.add(
            "d-none"
        );

    }


    if (content) {

        content.classList.add(
            "d-none"
        );

    }


    if (notFound) {

        notFound.classList.remove(
            "d-none"
        );

    }

}


/* =========================================================
   FORMAT CATEGORY
========================================================= */

function formatCategory(category) {

    if (!category) {
        return "Other";
    }


    const value =
        normalizeValue(category);


    const categories = {

        WATER:
            "Water Supply",

        ROADS:
            "Roads",

        ELECTRICITY:
            "Electricity",

        SANITATION:
            "Sanitation",

        HEALTH:
            "Health",

        OTHER:
            "Other"

    };


    return categories[value] ||
        formatStatus(value);

}


/* =========================================================
   FORMAT STATUS
========================================================= */

function formatStatus(value) {

    if (!value) {
        return "Unknown";
    }


    return String(value)

        .toLowerCase()

        .split("_")

        .map(function (word) {

            return (
                word.charAt(0).toUpperCase() +
                word.slice(1)
            );

        })

        .join(" ");

}


/* =========================================================
   NORMALIZE VALUE
========================================================= */

function normalizeValue(value) {

    if (
        value === null ||
        value === undefined
    ) {

        return "";

    }


    return String(value)
        .trim()
        .toUpperCase();

}


/* =========================================================
   FORMAT DATE
========================================================= */

function formatDate(value) {

    if (!value) {
        return "—";
    }


    const date =
        new Date(value);


    if (
        Number.isNaN(
            date.getTime()
        )
    ) {

        return String(value);

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
   SET TEXT
========================================================= */

function setText(
    elementId,
    value
) {

    const element =
        document.getElementById(
            elementId
        );


    if (!element) {
        return;
    }


    element.textContent =
        value === null ||
        value === undefined ||
        value === ""
            ? "—"
            : value;

}


/* =========================================================
   ESCAPE HTML
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