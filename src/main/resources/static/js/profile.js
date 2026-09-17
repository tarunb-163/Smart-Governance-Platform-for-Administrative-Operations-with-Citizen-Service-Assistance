/* =========================================================
   CIVICPULSE - OFFICER PROFILE
   profile.js

   TEMPORARY FRONTEND VERSION

   This version:
   - Loads temporary officer profile information
   - Allows profile editing
   - Saves changes to localStorage
   - Handles change-password UI
   - Handles logout
   - Does NOT connect to the backend yet

   Later this will connect to Spring Boot APIs.
========================================================= */


/* =========================================================
   TEMPORARY PROFILE DATA (DEFAULTS)
========================================================= */

const defaultOfficerProfile = {

    fullName: "Officer One",

    employeeId: "EMP001",

    email: "officer1@civicpulse.com",

    phone: "+91 9876543210",

    designation: "Senior Field Officer",

    department: "Roads / Public Works Department",

    assignedArea: "Central Ward & Sector 4",

    officeLocation: "Municipal PWD Headquarters, Zone 1",

    address: "Room 204, PWD Administrative Block, Civic Center, Andhra Pradesh",

    role: "OFFICER",

    jurisdiction: "Municipal North & Central Zones",

    joinedDate: "2026-03-16"

};


/* =========================================================
   CURRENT PROFILE
========================================================= */

let currentOfficerProfile = null;


/* =========================================================
   PAGE LOAD
========================================================= */

document.addEventListener(
    "DOMContentLoaded",
    function () {

        initializeProfile();

    }
);


/* =========================================================
   INITIALIZE
========================================================= */

function initializeProfile() {

    loadProfile();

    initializeProfileForm();

    initializePasswordForm();

    initializeSecurityActions();

}


/* =========================================================
   READ INITIAL PROFILE FROM DOM
========================================================= */

function readProfileFromDOM() {

    const dom = {};

    const fields = [
        "fullName",
        "employeeId",
        "email",
        "phone",
        "designation",
        "department",
        "assignedArea",
        "officeLocation",
        "address"
    ];

    fields.forEach(function (fieldId) {
        const val = getInputValue(fieldId);
        if (val && val.trim() !== "") {
            dom[fieldId] = val.trim();
        }
    });

    const roleEl = document.getElementById("officialRole");
    if (roleEl && roleEl.textContent.trim() && roleEl.textContent.trim() !== "—") {
        dom.role = roleEl.textContent.trim();
    }

    const jurisEl = document.getElementById("officialJurisdiction");
    if (jurisEl && jurisEl.textContent.trim() && jurisEl.textContent.trim() !== "—") {
        dom.jurisdiction = jurisEl.textContent.trim();
    }

    return dom;

}


/* =========================================================
   LOAD PROFILE
========================================================= */

async function loadProfile() {

    const domProfile =
        readProfileFromDOM();

    const storedProfile =
        getStoredProfile();

    currentOfficerProfile = {
        ...defaultOfficerProfile,
        ...domProfile,
        ...(storedProfile || {})
    };

    populateProfile(
        currentOfficerProfile
    );

    /*
     * Fetch profile asynchronously from backend API
     */
    try {

        const response =
            await fetch("/api/officer/profile");

        if (response.ok) {

            const apiData =
                await response.json();

            if (apiData && typeof apiData === "object") {

                const validApi = {};

                for (const [k, v] of Object.entries(apiData)) {

                    if (v !== null && v !== undefined && String(v).trim() !== "") {

                        validApi[k] = v;

                    }

                }

                currentOfficerProfile = {
                    ...defaultOfficerProfile,
                    ...currentOfficerProfile,
                    ...validApi
                };

                populateProfile(
                    currentOfficerProfile
                );

                saveProfileToStorage(
                    currentOfficerProfile
                );

            }

        }

    } catch (err) {

        console.log(
            "Using default / DOM officer profile:",
            err
        );

    }

}


/* =========================================================
   GET STORED PROFILE
========================================================= */

function getStoredProfile() {

    try {

        const stored =
            localStorage.getItem(
                "civicpulse_officer_profile"
            );


        if (!stored) {

            return null;

        }


        return JSON.parse(
            stored
        );

    } catch (error) {

        console.error(
            "Unable to load profile:",
            error
        );


        return null;

    }

}


/* =========================================================
   POPULATE PROFILE
========================================================= */

function populateProfile(
    profile
) {

    const safeProfile = {
        ...defaultOfficerProfile,
        ...(profile || {})
    };


    /* =====================================================
       FORM FIELDS
    ====================================================== */

    setInputValue(
        "fullName",
        safeProfile.fullName || defaultOfficerProfile.fullName
    );


    setInputValue(
        "employeeId",
        safeProfile.employeeId || defaultOfficerProfile.employeeId
    );


    setInputValue(
        "email",
        safeProfile.email || defaultOfficerProfile.email
    );


    setInputValue(
        "phone",
        safeProfile.phone || defaultOfficerProfile.phone
    );


    setInputValue(
        "designation",
        safeProfile.designation || defaultOfficerProfile.designation
    );


    setInputValue(
        "department",
        safeProfile.department || defaultOfficerProfile.department
    );


    setInputValue(
        "assignedArea",
        safeProfile.assignedArea || defaultOfficerProfile.assignedArea
    );


    setInputValue(
        "officeLocation",
        safeProfile.officeLocation || defaultOfficerProfile.officeLocation
    );


    setInputValue(
        "address",
        safeProfile.address || defaultOfficerProfile.address
    );


    /* =====================================================
       PROFILE CARD
    ====================================================== */

    const displayName =
        safeProfile.fullName ||
        defaultOfficerProfile.fullName;


    setText(
        "profileDisplayName",
        displayName
    );


    setText(
        "profileRole",
        safeProfile.designation ||
        safeProfile.role ||
        defaultOfficerProfile.designation
    );


    setText(
        "profileOfficerId",
        safeProfile.employeeId ||
        defaultOfficerProfile.employeeId
    );


    setText(
        "profileDepartment",
        safeProfile.department ||
        defaultOfficerProfile.department
    );


    setText(
        "profileArea",
        safeProfile.assignedArea ||
        defaultOfficerProfile.assignedArea
    );


    setText(
        "profileJoinedDate",
        safeProfile.joinedDate
            ? formatDate(
                safeProfile.joinedDate
            )
            : "16 Mar 2026"
    );


    /* =====================================================
       OFFICIAL INFORMATION
    ====================================================== */

    setText(
        "officialRole",
        safeProfile.role ||
        defaultOfficerProfile.role
    );


    setText(
        "officialDepartment",
        safeProfile.department ||
        defaultOfficerProfile.department
    );


    setText(
        "officialJurisdiction",
        safeProfile.jurisdiction ||
        safeProfile.assignedArea ||
        defaultOfficerProfile.jurisdiction
    );


    /* =====================================================
       AVATAR
    ====================================================== */

    updateAvatar(
        displayName
    );

}


/* =========================================================
   PROFILE FORM
========================================================= */

function initializeProfileForm() {

    const form =
        document.getElementById(
            "profileForm"
        );


    if (!form) {

        return;

    }


    form.addEventListener(
        "submit",
        handleProfileSubmit
    );


    const resetButton =
        document.getElementById(
            "resetProfileButton"
        );


    if (resetButton) {

        resetButton.addEventListener(
            "click",
            function () {

                populateProfile(
                    currentOfficerProfile
                );

                clearFormValidation();

            }
        );

    }

}


/* =========================================================
   PROFILE SUBMIT
========================================================= */

async function handleProfileSubmit(
    event
) {

    event.preventDefault();


    if (!validateProfileForm()) {

        return;

    }


    const updatedProfile =
        collectProfileFormData();


    currentOfficerProfile = {
        ...defaultOfficerProfile,
        ...currentOfficerProfile,
        ...updatedProfile
    };


    saveProfileToStorage(
        currentOfficerProfile
    );


    populateProfile(
        currentOfficerProfile
    );


    /*
     * Sync with Spring Boot API
     */
    try {

        const response =
            await fetch(
                "/api/officer/profile",
                {
                    method: "POST",
                    headers: {
                        "Content-Type": "application/json"
                    },
                    body: JSON.stringify(
                        currentOfficerProfile
                    )
                }
            );

        if (response.ok) {

            const result =
                await response.json();

            if (result && typeof result === "object") {

                currentOfficerProfile = {
                    ...currentOfficerProfile,
                    ...result
                };

                saveProfileToStorage(
                    currentOfficerProfile
                );

                populateProfile(
                    currentOfficerProfile
                );

            }

        }

    } catch (err) {

        console.warn(
            "Backend sync skipped:",
            err
        );

    }


    showProfileSuccess();

}


/* =========================================================
   VALIDATE PROFILE
========================================================= */

function validateProfileForm() {

    const fullName =
        document.getElementById(
            "fullName"
        );


    const email =
        document.getElementById(
            "email"
        );


    let valid = true;


    /*
     * Full name
     */

    if (
        fullName &&
        !fullName.value.trim()
    ) {

        fullName.classList.add(
            "is-invalid"
        );

        valid = false;

    } else if (fullName) {

        fullName.classList.remove(
            "is-invalid"
        );

    }


    /*
     * Email
     */

    if (
        email &&
        email.value.trim()
    ) {

        const emailPattern =
            /^[^\s@]+@[^\s@]+\.[^\s@]+$/;


        if (
            !emailPattern.test(
                email.value.trim()
            )
        ) {

            email.classList.add(
                "is-invalid"
            );

            valid = false;

        } else {

            email.classList.remove(
                "is-invalid"
            );

        }

    }


    if (!valid) {

        showProfileMessage(
            "Please enter valid profile information.",
            "error"
        );

    }


    return valid;

}


/* =========================================================
   COLLECT FORM DATA
========================================================= */

function collectProfileFormData() {

    return {

        fullName:
            getInputValue(
                "fullName"
            ),

        employeeId:
            getInputValue(
                "employeeId"
            ),

        email:
            getInputValue(
                "email"
            ),

        phone:
            getInputValue(
                "phone"
            ),

        designation:
            getInputValue(
                "designation"
            ),

        department:
            getInputValue(
                "department"
            ),

        assignedArea:
            getInputValue(
                "assignedArea"
            ),

        officeLocation:
            getInputValue(
                "officeLocation"
            ),

        address:
            getInputValue(
                "address"
            )

    };

}


/* =========================================================
   SAVE PROFILE
========================================================= */

function saveProfileToStorage(
    profile
) {

    try {

        localStorage.setItem(
            "civicpulse_officer_profile",
            JSON.stringify(
                profile
            )
        );

    } catch (error) {

        console.error(
            "Unable to save profile:",
            error
        );

    }

}


/* =========================================================
   PASSWORD FORM
========================================================= */

function initializePasswordForm() {

    const form =
        document.getElementById(
            "changePasswordForm"
        );


    if (!form) {

        return;

    }


    form.addEventListener(
        "submit",
        handlePasswordSubmit
    );

}


/* =========================================================
   SECURITY ACTIONS
========================================================= */




/* =========================================================
   OPEN CHANGE PASSWORD MODAL
========================================================= */

function openChangePasswordModal() {

    const modalElement =
        document.getElementById(
            "changePasswordModal"
        );


    if (
        !modalElement ||
        typeof bootstrap ===
        "undefined"
    ) {

        return;

    }


    const modal =
        new bootstrap.Modal(
            modalElement
        );


    modal.show();

}


/* =========================================================
   CHANGE PASSWORD
========================================================= */

function handlePasswordSubmit(
    event
) {

    event.preventDefault();


    const currentPassword =
        getInputValue(
            "currentPassword"
        );


    const newPassword =
        getInputValue(
            "newPassword"
        );


    const confirmPassword =
        getInputValue(
            "confirmPassword"
        );


    /*
     * Temporary validation only.
     *
     * We do NOT store passwords in localStorage.
     */


    if (!currentPassword) {

        showProfileMessage(
            "Enter your current password.",
            "error"
        );

        return;

    }


    if (
        newPassword.length < 8
    ) {

        showProfileMessage(
            "New password must contain at least 8 characters.",
            "error"
        );

        return;

    }


    if (
        newPassword !==
        confirmPassword
    ) {

        showProfileMessage(
            "New password and confirmation do not match.",
            "error"
        );

        return;

    }


    /*
     * Backend authentication will handle
     * the actual password change.
     */

    showProfileMessage(
        "Password update will be connected to the backend.",
        "success"
    );


    const form =
        document.getElementById(
            "changePasswordForm"
        );


    if (form) {

        form.reset();

    }


    const modalElement =
        document.getElementById(
            "changePasswordModal"
        );


    if (
        modalElement &&
        typeof bootstrap !==
        "undefined"
    ) {

        const modal =
            bootstrap.Modal.getInstance(
                modalElement
            );


        if (modal) {

            modal.hide();

        }

    }

}


/* =========================================================
   LOGOUT
========================================================= */



/* =========================================================
   SUCCESS MODAL
========================================================= */

function showProfileSuccess() {

    const modalElement =
        document.getElementById(
            "profileSuccessModal"
        );


    if (
        !modalElement ||
        typeof bootstrap ===
        "undefined"
    ) {

        showProfileMessage(
            "Profile updated successfully.",
            "success"
        );

        return;

    }


    const modal =
        new bootstrap.Modal(
            modalElement
        );


    modal.show();

}


/* =========================================================
   FORM MESSAGE
========================================================= */

function showProfileMessage(
    message,
    type
) {

    const existing =
        document.querySelector(
            ".profile-form-message"
        );


    if (existing) {

        existing.remove();

    }


    const messageElement =
        document.createElement(
            "div"
        );


    messageElement.className =
        "profile-form-message " +
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
            "profileForm"
        );


    if (form) {

        form.prepend(
            messageElement
        );

    } else {

        document.body.prepend(
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
   UPDATE AVATAR
========================================================= */

function updateAvatar(
    name
) {

    const initialsElement =
        document.getElementById(
            "profileInitials"
        );


    if (!initialsElement) {

        return;

    }


    const initials =
        getInitials(
            name
        );


    initialsElement.textContent =
        initials;

}


/* =========================================================
   GET INITIALS
========================================================= */

function getInitials(
    name
) {

    if (
        !name ||
        !name.trim()
    ) {

        return "O";

    }


    const words =
        name
            .trim()
            .split(/\s+/)
            .filter(Boolean);


    if (words.length === 1) {

        return words[0]
            .substring(0, 2)
            .toUpperCase();

    }


    return (
        words[0].charAt(0) +
        words[
        words.length - 1
            ].charAt(0)
    ).toUpperCase();

}


/* =========================================================
   SET INPUT VALUE
========================================================= */

function setInputValue(
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


    element.value =
        value === null ||
        value === undefined
            ? ""
            : value;

}


/* =========================================================
   GET INPUT VALUE
========================================================= */

function getInputValue(
    elementId
) {

    const element =
        document.getElementById(
            elementId
        );


    if (!element) {

        return "";

    }


    return element.value.trim();

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
   CLEAR FORM VALIDATION
========================================================= */

function clearFormValidation() {

    const fields =
        document.querySelectorAll(
            "#profileForm .form-control"
        );


    fields.forEach(
        function (field) {

            field.classList.remove(
                "is-invalid",
                "is-valid"
            );

        }
    );

}


/* =========================================================
   FORMAT DATE
========================================================= */

function formatDate(
    value
) {

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

/* =========================================================
   CHANGE PASSWORD MODAL HANDLER
========================================================= */

document.addEventListener("DOMContentLoaded", function () {
    const changePasswordForm = document.getElementById("changePasswordForm");
    const modalAlert = document.getElementById("modalPasswordAlert");
    const submitBtn = document.getElementById("modalUpdatePasswordBtn");

    if (changePasswordForm) {
        changePasswordForm.addEventListener("submit", async function (e) {
            e.preventDefault();

            const currentPassword = document.getElementById("currentPassword").value.trim();
            const newPassword = document.getElementById("newPassword").value;
            const confirmPassword = document.getElementById("confirmPassword").value;

            if (modalAlert) {
                modalAlert.className = "alert d-none mb-3";
                modalAlert.textContent = "";
            }

            if (!currentPassword) {
                showModalAlert("Please enter your current password.", "danger");
                return;
            }
            if (!newPassword || newPassword.length < 6) {
                showModalAlert("New password must be at least 6 characters in length.", "danger");
                return;
            }
            if (newPassword !== confirmPassword) {
                showModalAlert("New password and confirm password do not match.", "danger");
                return;
            }

            if (submitBtn) {
                submitBtn.disabled = true;
                submitBtn.innerHTML = '<span class="spinner-border spinner-border-sm me-2"></span> Updating...';
            }

            try {
                const response = await fetch("/api/officer/change-password", {
                    method: "POST",
                    headers: {
                        "Content-Type": "application/json"
                    },
                    body: JSON.stringify({
                        currentPassword: currentPassword,
                        newPassword: newPassword,
                        confirmPassword: confirmPassword
                    })
                });

                const data = await response.json();

                if (response.ok && data.success) {
                    showModalAlert(data.message || "Password updated successfully!", "success");
                    changePasswordForm.reset();
                    setTimeout(() => {
                        const modalEl = document.getElementById("changePasswordModal");
                        if (modalEl && window.bootstrap) {
                            const modalInstance = bootstrap.Modal.getInstance(modalEl) || new bootstrap.Modal(modalEl);
                            modalInstance.hide();
                        }
                        window.location.reload();
                    }, 1200);
                } else {
                    showModalAlert(data.message || "Failed to update password. Please check your credentials.", "danger");
                }
            } catch (err) {
                console.error("Change password error:", err);
                changePasswordForm.submit();
            } finally {
                if (submitBtn) {
                    submitBtn.disabled = false;
                    submitBtn.innerHTML = '<i class="bi bi-check-lg"></i> Update Password';
                }
            }
        });
    }

    function showModalAlert(message, type) {
        if (!modalAlert) return;
        modalAlert.className = `alert alert-${type} mb-3`;
        modalAlert.textContent = message;
    }
});