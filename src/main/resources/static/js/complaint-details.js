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


    setText(
        "citizenId",
        complaint.citizenId
    );


    setText(
        "citizenAddress",
        complaint.citizenAddress || complaint.location
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

    renderAttachments(
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
   RENDER ATTACHMENTS / EVIDENCE
========================================================= */

function renderAttachments(complaint) {
    const emptyContainer = document.getElementById("attachmentsEmpty");
    const listContainer = document.getElementById("attachmentsList");

    if (!emptyContainer || !listContainer) return;

    // Collect attachments from complaint.attachments and complaint.imagePath
    let attachments = [];
    if (complaint && complaint.attachments && Array.isArray(complaint.attachments)) {
        attachments = [...complaint.attachments];
    }
    if (complaint && complaint.imagePath && complaint.imagePath.trim() !== "") {
        const path = complaint.imagePath.trim();
        const exists = attachments.some(a =>
            a.fileUrl === path || (a.fileUrl && a.fileUrl.endsWith(path)) || (a.filePath && a.filePath.endsWith(path))
        );
        if (!exists) {
            const fileName = path.includes('/') ? path.substring(path.lastIndexOf('/') + 1) : path;
            attachments.push({
                fileName: fileName,
                fileType: "image/jpeg",
                fileUrl: path,
                uploadedAt: complaint.createdAt
            });
        }
    }

    if (attachments.length === 0) {
        emptyContainer.classList.remove("d-none");
        listContainer.classList.add("d-none");
        listContainer.innerHTML = "";
        return;
    }

    emptyContainer.classList.add("d-none");
    listContainer.classList.remove("d-none");
    listContainer.innerHTML = "";

    attachments.forEach(function (att) {
        const fileUrl = att.fileUrl || att.filePath || (att.storedFileName ? "/uploads/" + att.storedFileName : "#");
        const fileName = att.fileName || "Evidence Document";
        const fileType = (att.fileType || "").toLowerCase();
        const isImg = isImageAttachment(fileName, fileType);
        const formattedDate = att.uploadedAt ? formatDate(att.uploadedAt) : (complaint.createdAt ? formatDate(complaint.createdAt) : "Attached");
        const fileSizeText = att.fileSize ? formatFileSize(att.fileSize) : (isImg ? "Photo Evidence" : "Attachment");

        const item = document.createElement("div");
        item.className = "attachment-card-item";

        if (isImg) {
            item.innerHTML = `
                <div class="attachment-image-wrapper" onclick="openEvidenceModal('${escapeHtml(fileUrl)}', '${escapeHtml(fileName)}')">
                    <img src="${escapeHtml(fileUrl)}" alt="${escapeHtml(fileName)}" class="attachment-thumb-img" onerror="this.onerror=null; this.src='/images/placeholder-image.png';">
                    <div class="attachment-thumb-overlay">
                        <i class="bi bi-zoom-in"></i>
                    </div>
                </div>
                <div class="attachment-details">
                    <strong class="attachment-title" title="${escapeHtml(fileName)}">${escapeHtml(fileName)}</strong>
                    <div class="attachment-meta">
                        <span class="badge bg-primary-subtle text-primary border border-primary-subtle me-1">Photo Evidence</span>
                        <span><i class="bi bi-clock me-1"></i>${escapeHtml(formattedDate)}</span>
                        ${att.fileSize ? `<span class="ms-2"><i class="bi bi-hdd me-1"></i>${escapeHtml(fileSizeText)}</span>` : ''}
                    </div>
                </div>
                <div class="attachment-actions">
                    <button type="button" class="btn btn-sm btn-outline-primary" onclick="openEvidenceModal('${escapeHtml(fileUrl)}', '${escapeHtml(fileName)}')">
                        <i class="bi bi-eye me-1"></i> View Photo
                    </button>
                    <a href="${escapeHtml(fileUrl)}" target="_blank" download class="btn btn-sm btn-outline-secondary" title="Download Image">
                        <i class="bi bi-download"></i>
                    </a>
                </div>
            `;
        } else {
            item.innerHTML = `
                <div class="attachment-icon file-doc">
                    <i class="bi bi-file-earmark-text"></i>
                </div>
                <div class="attachment-details">
                    <strong class="attachment-title" title="${escapeHtml(fileName)}">${escapeHtml(fileName)}</strong>
                    <div class="attachment-meta">
                        <span class="badge bg-secondary-subtle text-secondary me-1">Document</span>
                        <span><i class="bi bi-clock me-1"></i>${escapeHtml(formattedDate)}</span>
                    </div>
                </div>
                <div class="attachment-actions">
                    <a href="${escapeHtml(fileUrl)}" target="_blank" class="btn btn-sm btn-outline-primary">
                        <i class="bi bi-box-arrow-up-right me-1"></i> Open
                    </a>
                </div>
            `;
        }
        listContainer.appendChild(item);
    });
}

function isImageAttachment(fileName, fileType) {
    if (fileType && fileType.startsWith("image/")) return true;
    const lower = (fileName || "").toLowerCase();
    return lower.endsWith(".jpg") || lower.endsWith(".jpeg") || lower.endsWith(".png") || lower.endsWith(".webp") || lower.endsWith(".gif");
}

function formatFileSize(bytes) {
    if (!bytes || isNaN(bytes)) return "";
    if (bytes < 1024) return bytes + " B";
    if (bytes < 1048576) return (bytes / 1024).toFixed(1) + " KB";
    return (bytes / 1048576).toFixed(1) + " MB";
}

function openEvidenceModal(imageUrl, caption) {
    const modalImg = document.getElementById("modalEvidenceImg");
    const modalCaption = document.getElementById("modalEvidenceCaption");
    const downloadBtn = document.getElementById("modalEvidenceDownloadBtn");

    if (modalImg) modalImg.src = imageUrl;
    if (modalCaption) modalCaption.innerText = caption || "Complaint Evidence";
    if (downloadBtn) {
        downloadBtn.href = imageUrl;
    }

    const modalEl = document.getElementById('evidenceImageModal');
    if (modalEl && window.bootstrap) {
        const modal = bootstrap.Modal.getOrCreateInstance(modalEl);
        modal.show();
    } else {
        window.open(imageUrl, '_blank');
    }
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