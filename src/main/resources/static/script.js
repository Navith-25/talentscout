document.addEventListener("DOMContentLoaded", () => {
    fetchBestCandidate();
});

const cvFileInput = document.getElementById('cvFile');
const selectedFileName = document.getElementById('selectedFileName');
const uploadBtn = document.getElementById('uploadBtn');
const uploadArea = document.getElementById('uploadArea');
let currentFile = null;

cvFileInput.addEventListener('change', function(e) {
    handleFiles(e.target.files);
});

uploadArea.addEventListener('dragover', (e) => {
    e.preventDefault();
    uploadArea.classList.add('dragover');
});

uploadArea.addEventListener('dragleave', () => {
    uploadArea.classList.remove('dragover');
});

uploadArea.addEventListener('drop', (e) => {
    e.preventDefault();
    uploadArea.classList.remove('dragover');
    handleFiles(e.dataTransfer.files);
});

function handleFiles(files) {
    if (files.length > 0) {
        if(files[0].type === "application/pdf") {
            currentFile = files[0];
            selectedFileName.textContent = currentFile.name;
            uploadBtn.disabled = false;
        } else {
            alert("Please upload a PDF file only.");
            currentFile = null;
            selectedFileName.textContent = "No file selected";
            uploadBtn.disabled = true;
        }
    }
}

async function uploadCV() {
    if (!currentFile) return;

    uploadBtn.innerHTML = "⏳ Parsing Resume & Calculating Score...";
    uploadBtn.disabled = true;

    const formData = new FormData();
    formData.append("file", currentFile);

    try {
        const response = await fetch('/api/candidates/upload', {
            method: 'POST',
            body: formData
        });

        const resultText = await response.text();

        if (response.ok) {
            currentFile = null;
            selectedFileName.textContent = "No file selected";
            cvFileInput.value = "";
            uploadBtn.innerHTML = "Process AI Scoring & Add";

            fetchBestCandidate();
        } else {
            alert("Upload Failed: " + resultText);
            uploadBtn.innerHTML = "Process AI Scoring & Add";
            uploadBtn.disabled = false;
        }
    } catch (error) {
        console.error("Error uploading CV:", error);
        alert("Network error occurred.");
        uploadBtn.innerHTML = "Process AI Scoring & Add";
        uploadBtn.disabled = false;
    }
}

async function fetchBestCandidate() {
    const isBlindMode = document.getElementById('blindModeToggle').checked;
    const endpoint = isBlindMode ? '/api/candidates/best-blind' : '/api/candidates/best';
    const nextBtn = document.getElementById('nextBtn');

    try {
        const response = await fetch(endpoint);
        const candidateDetailsDiv = document.getElementById('candidateDetails');

        if (response.status === 204) {
            candidateDetailsDiv.innerHTML = '<p class="empty-msg">Queue is empty. Waiting for CV uploads...</p>';
            nextBtn.style.display = 'none';
            return;
        }

        const candidate = await response.json();

        const displayName = candidate.name ? candidate.name : candidate.candidateAlias;
        const skillsList = (candidate.skills && candidate.skills.length > 0) ? candidate.skills.join(', ') : "None Detected";

        candidateDetailsDiv.innerHTML = `
            <h2>${displayName}</h2>
            <p><strong>Suitability Score:</strong> <span class="score-badge">${candidate.suitabilityScore}</span></p>
            <p><strong>Email:</strong> ${candidate.email || "Not Detected"}</p>
            <p><strong>Phone:</strong> ${candidate.phone || "Not Detected"}</p>
            <p><strong>Experience:</strong> ${candidate.experienceYears} Years</p>
            <p><strong>Education:</strong> ${candidate.educationLevel || "Not Detected"}</p>
            <p><strong>Skills Found:</strong> ${skillsList}</p>
        `;
        nextBtn.style.display = 'block';
    } catch (error) {
        console.error("Error fetching candidate:", error);
    }
}

async function removeAndFetchNext() {
    try {
        await fetch('/api/candidates/next');
        fetchBestCandidate();
    } catch (error) {
        console.error("Error fetching next:", error);
    }
}

async function triggerStarvation() {
    try {
        const response = await fetch('/api/candidates/trigger-starvation', { method: 'POST' });
        if (response.ok) {
            alert("Anti-Starvation Algorithm Executed! Older applications in the queue received a +5.0 score boost.");
            fetchBestCandidate();
        }
    } catch (error) {
        console.error("Error triggering starvation:", error);
    }
}