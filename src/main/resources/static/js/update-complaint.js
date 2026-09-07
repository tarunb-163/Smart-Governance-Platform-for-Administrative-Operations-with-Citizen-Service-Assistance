/* =========================================================
   CIVICPULSE - UPDATE COMPLAINT
   update-complaint.js

   TEMPORARY FRONTEND TEST VERSION

   This version:
   - Reads complaint ID from URL
   - Loads temporary complaint data
   - Displays current complaint information
   - Allows status/priority/resolution updates
   - Validates the form
   - Handles file selection
   - Saves changes in browser memory
   - Redirects back to complaint details

   Later this will connect to:
   PUT /api/officer/complaints/{id}
========================================================= */


/* =========================================================
   TEST COMPLAINT DATA
========================================================= */

const updateTestComplaints = [

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
        createdAt: "2026-08-01T09:30:00",
        resolution: "",
        remarks: ""
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
        createdAt: "2026-08-02T10:15:00",
        resolution: "",
        remarks: ""
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
        createdAt: "2026-08-03T11:00:00",
        resolution: "",
        remarks: ""
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
        createdAt: "2026-08-04T08:45:00",
        resolution: "",
        remarks: ""
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
        createdAt: "2026-08-05T12:20:00",
        resolution: "",
        remarks: ""
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
        createdAt: "2026-08-06T14:10:00",
        resolution: "",
        remarks: ""
    }

];


/* =========================================================
   CURRENT COMPLAINT
========================================================= */

let currentUpdateComplaint = null;


/* =========================================================
   SELECTED FILES
========================================================= */

let selectedFiles = [];


/* =========================================================
   PAGE LOAD
========================================================= */

document.addEventListener(
    "DOMContentLoaded",
    function () {

        initializeUpdateComplaint();

    }
);


/* =========================================================
   INITIALIZE
========================================================= */

function initializeUpdateComplaint() {

    const complaintId =
        getComplaintIdFromUrl();


    if (!complaintId) {

        showUpdateNotFound();

        return;

    }


    loadComplaintForUpdate(
        complaintId
    );


    initializeFormEvents();

}


/* =========================================================
   GET COMPLAINT ID
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

function loadComplaintForUpdate(
    complaintId
) {

    showUpdateLoading();


    fetch('/api/officer/complaints/' + encodeURIComponent(complaintId))
        .then(response => {
            if (!response.ok) throw new Error("Not found");
            return response.json();
        })
        .then(complaint => {
            currentUpdateComplaint = complaint;
            populateCurrentComplaint(complaint);
            populateUpdateForm(complaint);
            showUpdateContent();
        })
        .catch(() => {
            const complaint = updateTestComplaints.find(item => item.id === complaintId);
            if (!complaint) {
                showUpdateNotFound();
                return;
            }
            currentUpdateComplaint = complaint;
            populateCurrentComplaint(complaint);
            populateUpdateForm(complaint);
            showUpdateContent();
        });

}


/* =========================================================
   POPULATE CURRENT COMPLAINT
========================================================= */

function populateCurrentComplaint(
    complaint
) {

    setText(
        "currentComplaintId",
        complaint.id
    );


    setText(
        "currentComplaintCategory",
        formatCategory(
            complaint.category
        )
    );


    setText(
        "currentComplaintDepartment",
        complaint.department
    );


    renderCurrentStatus(
        complaint.status
    );

}


/* =========================================================
   POPULATE FORM
========================================================= */

function populateUpdateForm(
    complaint
) {

    const statusInput =
        document.getElementById(
            "complaintStatusInput"
        );


    const priorityInput =
        document.getElementById(
            "priorityInput"
        );


    const resolutionInput =
        document.getElementById(
            "resolutionDetails"
        );


    const remarksInput =
        document.getElementById(
            "officerRemarks"
        );


    if (statusInput) {

        statusInput.value =
            complaint.status || "";

    }


    if (priorityInput) {

        priorityInput.value =
            complaint.priority || "";

    }


    if (resolutionInput) {

        resolutionInput.value =
            complaint.resolution || "";

        updateCharacterCounter(
            resolutionInput,
            "resolutionCounter"
        );

    }


    if (remarksInput) {

        remarksInput.value =
            complaint.remarks || "";

        updateCharacterCounter(
            remarksInput,
            "remarksCounter"
        );

    }


    updateCancelLink(
        complaint.id
    );

}


/* =========================================================
   CURRENT STATUS
========================================================= */

function renderCurrentStatus(
    status
) {

    const container =
        document.getElementById(
            "currentComplaintStatus"
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
   FORM EVENTS
========================================================= */

function initializeFormEvents() {

    const form =
        document.getElementById(
            "updateComplaintForm"
        );


    if (form) {

        form.addEventListener(
            "submit",
            handleFormSubmit
        );

    }


    const resetButton =
        document.getElementById(
            "resetUpdateForm"
        );


    if (resetButton) {

        resetButton.addEventListener(
            "click",
            resetUpdateForm
        );

    }


    const resolutionInput =
        document.getElementById(
            "resolutionDetails"
        );


    if (resolutionInput) {

        resolutionInput.addEventListener(
            "input",
            function () {

                updateCharacterCounter(
                    this,
                    "resolutionCounter"
                );

            }
        );

    }


    const remarksInput =
        document.getElementById(
            "officerRemarks"
        );


    if (remarksInput) {

        remarksInput.addEventListener(
            "input",
            function () {

                updateCharacterCounter(
                    this,
                    "remarksCounter"
                );

            }
        );

    }


    const fileInput =
        document.getElementById(
            "resolutionAttachments"
        );


    if (fileInput) {

        fileInput.addEventListener(
            "change",
            handleFileSelection
        );

    }

}


/* =========================================================
   FORM SUBMIT
========================================================= */

function handleFormSubmit(
    event
) {

    event.preventDefault();


    const form =
        event.target;


    if (!validateForm(form)) {

        return;

    }


    saveComplaintChanges();

}


/* =========================================================
   FORM VALIDATION
========================================================= */

function validateForm(
    form
) {

    const statusInput =
        document.getElementById(
            "complaintStatusInput"
        );


    const priorityInput =
        document.getElementById(
            "priorityInput"
        );


    let valid = true;


    if (
        !statusInput ||
        !statusInput.value
    ) {

        markInvalid(
            statusInput
        );

        valid = false;

    } else {

        markValid(
            statusInput
        );

    }


    if (
        !priorityInput ||
        !priorityInput.value
    ) {

        markInvalid(
            priorityInput
        );

        valid = false;

    } else {

        markValid(
            priorityInput
        );

    }


    if (!valid) {

        showFormMessage(
            "Please complete all required fields.",
            "error"
        );

        return false;

    }


    return true;

}


/* =========================================================
   MARK INVALID
========================================================= */

function markInvalid(
    element
) {

    if (!element) {
        return;
    }


    element.classList.add(
        "is-invalid"
    );

}


/* =========================================================
   MARK VALID
========================================================= */

function markValid(
    element
) {

    if (!element) {
        return;
    }


    element.classList.remove(
        "is-invalid"
    );

    element.classList.add(
        "is-valid"
    );

}


/* =========================================================
   SAVE CHANGES
========================================================= */

function saveComplaintChanges() {

    if (!currentUpdateComplaint) {

        showFormMessage(
            "Complaint could not be loaded.",
            "error"
        );

        return;

    }


    const statusInput =
        document.getElementById(
            "complaintStatusInput"
        );


    const priorityInput =
        document.getElementById(
            "priorityInput"
        );


    const resolutionInput =
        document.getElementById(
            "resolutionDetails"
        );


    const remarksInput =
        document.getElementById(
            "officerRemarks"
        );


    const saveButton =
        document.getElementById(
            "saveComplaintButton"
        );


    const newStatus =
        statusInput.value;


    const newPriority =
        priorityInput.value;


    const newResolution =
        resolutionInput
            ? resolutionInput.value.trim()
            : "";


    const newRemarks =
        remarksInput
            ? remarksInput.value.trim()
            : "";


    /* =====================================================
       UPDATE CURRENT OBJECT
    ====================================================== */

    currentUpdateComplaint.status =
        newStatus;


    currentUpdateComplaint.priority =
        newPriority;


    currentUpdateComplaint.resolution =
        newResolution;


    currentUpdateComplaint.remarks =
        newRemarks;


    /* =====================================================
       TEMPORARY BROWSER STORAGE
    ====================================================== */

    saveComplaintToBrowserStorage(
        currentUpdateComplaint
    );


    /* =====================================================
       BUTTON LOADING
    ====================================================== */

    if (saveButton) {

        saveButton.disabled = true;

        saveButton.innerHTML = `

            <span
                class="spinner-border spinner-border-sm"
                aria-hidden="true">
            </span>

            Saving...

        `;

    }


    /*
     * Simulate save operation.
     *
    fetch('/api/officer/complaints/' + encodeURIComponent(currentUpdateComplaint.id), {
        method: "PUT",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
            status: newStatus,
            priority: newPriority,
            resolution: newResolution,
            remarks: newRemarks
        })
    })
    .then(response => {
        if (!response.ok) throw new Error("Update failed");
        return response.json();
    })
    .catch(err => {
        console.warn("Backend update error, persisted locally:", err);
    })
    .finally(() => {
        if (saveButton) {
            saveButton.disabled = false;
            saveButton.innerHTML = `<i class="bi bi-check-lg"></i> Save Changes`;
        }
        showSuccessModal();
    });
}


/* =========================================================
   SAVE TO BROWSER STORAGE
========================================================= */

function saveComplaintToBrowserStorage(
    complaint
) {

    try {

        const storageKey =
            "civicpulse_test_complaints";


        let storedComplaints =
            JSON.parse(
                localStorage.getItem(
                    storageKey
                )
            );


        if (
            !Array.isArray(
                storedComplaints
            )
        ) {

            storedComplaints =
                updateTestComplaints.map(
                    function (item) {

                        return {
                            ...item
                        };

                    }
                );

        }


        const index =
            storedComplaints.findIndex(
                function (item) {

                    return item.id ===
                        complaint.id;

                }
            );


        if (index !== -1) {

            storedComplaints[index] = {
                ...storedComplaints[index],
                ...complaint
            };

        } else {

            storedComplaints.push(
                complaint
            );

        }


        localStorage.setItem(
            storageKey,
            JSON.stringify(
                storedComplaints
            )
        );


    } catch (error) {

        console.error(
            "Unable to save test complaint:",
            error
        );

    }

}


/* =========================================================
   RESET FORM
========================================================= */

function resetUpdateForm() {

    if (!currentUpdateComplaint) {
        return;
    }


    const statusInput =
        document.getElementById(
            "complaintStatusInput"
        );


    const priorityInput =
        document.getElementById(
            "priorityInput"
        );


    const resolutionInput =
        document.getElementById(
            "resolutionDetails"
        );


    const remarksInput =
        document.getElementById(
            "officerRemarks"
        );


    if (statusInput) {

        statusInput.value =
            currentUpdateComplaint.status || "";

        statusInput.classList.remove(
            "is-invalid",
            "is-valid"
        );

    }


    if (priorityInput) {

        priorityInput.value =
            currentUpdateComplaint.priority || "";

        priorityInput.classList.remove(
            "is-invalid",
            "is-valid"
        );

    }


    if (resolutionInput) {

        resolutionInput.value =
            currentUpdateComplaint.resolution || "";

        updateCharacterCounter(
            resolutionInput,
            "resolutionCounter"
        );

    }


    if (remarksInput) {

        remarksInput.value =
            currentUpdateComplaint.remarks || "";

        updateCharacterCounter(
            remarksInput,
            "remarksCounter"
        );

    }


    selectedFiles = [];

    renderSelectedFiles();

}


/* =========================================================
   CHARACTER COUNTER
========================================================= */

function updateCharacterCounter(
    input,
    counterId
) {

    const counter =
        document.getElementById(
            counterId
        );


    if (!input || !counter) {
        return;
    }


    counter.textContent =
        input.value.length;

}


/* =========================================================
   FILE SELECTION
========================================================= */

function handleFileSelection(
    event
) {

    const files =
        Array.from(
            event.target.files || []
        );


    selectedFiles =
        files;


    renderSelectedFiles();

}


/* =========================================================
   RENDER SELECTED FILES
========================================================= */

function renderSelectedFiles() {

    const container =
        document.getElementById(
            "selectedFiles"
        );


    if (!container) {
        return;
    }


    container.innerHTML = "";


    if (
        selectedFiles.length === 0
    ) {

        container.classList.add(
            "d-none"
        );

        return;

    }


    container.classList.remove(
        "d-none"
    );


    selectedFiles.forEach(
        function (file, index) {

            const item =
                document.createElement(
                    "div"
                );


            item.className =
                "selected-file-item";


            item.innerHTML = `

                <div class="selected-file-icon">

                    <i class="${getFileIcon(
                file.name
            )}"></i>

                </div>


                <div class="selected-file-info">

                    <strong>
                        ${escapeHtml(
                file.name
            )}
                    </strong>

                    <span>
                        ${formatFileSize(
                file.size
            )}
                    </span>

                </div>


                <button
                    type="button"
                    class="selected-file-remove"
                    title="Remove file">

                    <i class="bi bi-x"></i>

                </button>

            `;


            const removeButton =
                item.querySelector(
                    ".selected-file-remove"
                );


            if (removeButton) {

                removeButton.addEventListener(
                    "click",
                    function () {

                        removeSelectedFile(
                            index
                        );

                    }
                );

            }


            container.appendChild(
                item
            );

        }
    );

}


/* =========================================================
   REMOVE SELECTED FILE
========================================================= */

function removeSelectedFile(
    index
) {

    selectedFiles.splice(
        index,
        1
    );


    const fileInput =
        document.getElementById(
            "resolutionAttachments"
        );


    /*
     * Rebuild FileList using DataTransfer
     * where browser support is available.
     */

    if (fileInput) {

        try {

            const dataTransfer =
                new DataTransfer();


            selectedFiles.forEach(
                function (file) {

                    dataTransfer.items.add(
                        file
                    );

                }
            );


            fileInput.files =
                dataTransfer.files;

        } catch (error) {

            console.warn(
                "Unable to rebuild file input:",
                error
            );

        }

    }


    renderSelectedFiles();

}


/* =========================================================
   FILE ICON
========================================================= */

function getFileIcon(
    fileName
) {

    const extension =
        fileName
            .split(".")
            .pop()
            .toLowerCase();


    switch (extension) {

        case "pdf":

            return "bi bi-file-earmark-pdf";


        case "doc":

        case "docx":

            return "bi bi-file-earmark-word";


        case "jpg":

        case "jpeg":

        case "png":

            return "bi bi-file-earmark-image";


        default:

            return "bi bi-file-earmark";

    }

}


/* =========================================================
   FILE SIZE
========================================================= */

function formatFileSize(
    bytes
) {

    if (!bytes) {
        return "0 KB";
    }


    const units = [
        "Bytes",
        "KB",
        "MB",
        "GB"
    ];


    const index =
        Math.floor(
            Math.log(bytes) /
            Math.log(1024)
        );


    const size =
        bytes /
        Math.pow(
            1024,
            index
        );


    return (
            Math.round(
                size * 100
            ) / 100
        ) +
        " " +
        units[index];

}


/* =========================================================
   UPDATE CANCEL LINK
========================================================= */

function updateCancelLink(
    complaintId
) {

    const cancelButton =
        document.getElementById(
            "cancelUpdate"
        );


    if (cancelButton) {

        cancelButton.href =
            "/officer/complaint-details?id=" +
            encodeURIComponent(
                complaintId
            );

    }

}


/* =========================================================
   SUCCESS MODAL
========================================================= */

function showSuccessModal() {

    const modalElement =
        document.getElementById(
            "updateSuccessModal"
        );


    if (!modalElement) {

        redirectToDetails();

        return;

    }


    if (
        typeof bootstrap !==
        "undefined"
    ) {

        const modal =
            new bootstrap.Modal(
                modalElement
            );


        modalElement.addEventListener(
            "hidden.bs.modal",
            function () {

                redirectToDetails();

            },
            {
                once: true
            }
        );


        modal.show();

    } else {

        redirectToDetails();

    }

}


/* =========================================================
   REDIRECT TO DETAILS
========================================================= */

function redirectToDetails() {

    if (
        !currentUpdateComplaint
    ) {

        window.location.href =
            "/officer/complaints";

        return;

    }


    window.location.href =
        "/officer/complaint-details?id=" +
        encodeURIComponent(
            currentUpdateComplaint.id
        );

}


/* =========================================================
   SHOW LOADING
========================================================= */

function showUpdateLoading() {

    const loading =
        document.getElementById(
            "updateComplaintLoading"
        );


    const notFound =
        document.getElementById(
            "updateComplaintNotFound"
        );


    const content =
        document.getElementById(
            "updateComplaintContent"
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
   SHOW CONTENT
========================================================= */

function showUpdateContent() {

    const loading =
        document.getElementById(
            "updateComplaintLoading"
        );


    const notFound =
        document.getElementById(
            "updateComplaintNotFound"
        );


    const content =
        document.getElementById(
            "updateComplaintContent"
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

function showUpdateNotFound() {

    const loading =
        document.getElementById(
            "updateComplaintLoading"
        );


    const notFound =
        document.getElementById(
            "updateComplaintNotFound"
        );


    const content =
        document.getElementById(
            "updateComplaintContent"
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
   FORM MESSAGE
========================================================= */

function showFormMessage(
    message,
    type
) {

    const existing =
        document.querySelector(
            ".update-form-message"
        );


    if (existing) {
        existing.remove();
    }


    const messageElement =
        document.createElement(
            "div"
        );


    messageElement.className =
        "update-form-message " +
        (
            type === "error"
                ? "error"
                : "success"
        );


    messageElement.innerHTML = `

        <i class="${
        type === "error"
            ? "bi bi-exclamation-circle"
            : "bi bi-check-circle"
    }"></i>

        <span>
            ${escapeHtml(message)}
        </span>

    `;


    const form =
        document.getElementById(
            "updateComplaintForm"
        );


    if (form) {

        form.prepend(
            messageElement
        );

    }


    setTimeout(
        function () {

            if (
                messageElement &&
                messageElement.parentNode
            ) {

                messageElement.remove();

            }

        },
        4000
    );

}


/* =========================================================
   FORMAT CATEGORY
========================================================= */

function formatCategory(
    category
) {

    if (!category) {
        return "Other";
    }


    const value =
        normalizeValue(
            category
        );


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


    return (
        categories[value] ||
        formatStatus(value)
    );

}


/* =========================================================
   FORMAT STATUS
========================================================= */

function formatStatus(
    value
) {

    if (!value) {
        return "Unknown";
    }


    return String(value)

        .toLowerCase()

        .split("_")

        .map(
            function (word) {

                return (
                    word.charAt(0).toUpperCase() +
                    word.slice(1)
                );

            }
        )

        .join(" ");

}


/* =========================================================
   NORMALIZE VALUE
========================================================= */

function normalizeValue(
    value
) {

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

function escapeHtml(
    value
) {

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