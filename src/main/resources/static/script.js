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
            fetchQueue();
            return;
        }

        const candidate = await response.json();

        const displayName = candidate.name ? candidate.name : candidate.candidateAlias;
        const skillsList = (candidate.skills && candidate.skills.length > 0) ? candidate.skills.join(', ') : "None Detected";

        // Dynamic '--score' property eka inline style ekak widiyata pass karala thiyenawa
        candidateDetailsDiv.innerHTML = `
            <div class="score-circle" style="--score: ${candidate.suitabilityScore}" data-score="${candidate.suitabilityScore}"></div>
            <h2 style="text-align: center; margin-bottom: 1.5rem;">${displayName}</h2>

            <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 1rem; border-top: 1px solid var(--border); padding-top: 1rem;">
                <p><strong>📧 Email:</strong><br>${candidate.email || "N/A"}</p>
                <p><strong>📞 Phone:</strong><br>${candidate.phone || "N/A"}</p>
                <p><strong>💼 Exp:</strong><br>${candidate.experienceYears} Years</p>
                <p><strong>🎓 Edu:</strong><br>${candidate.educationLevel || "N/A"}</p>
            </div>

            <p style="margin-top: 1rem; padding: 10px; background: rgba(99, 102, 241, 0.05); border-radius: 12px;">
                <strong>🛠 Skills Found:</strong><br>
                <span style="color: var(--primary); font-weight: 500;">${skillsList}</span>
            </p>
        `;
        nextBtn.style.display = 'block';
    } catch (error) {
        console.error("Error fetching candidate:", error);
    }

    fetchQueue();
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

async function fetchQueue() {
    const isBlindMode = document.getElementById('blindModeToggle').checked;
    try {
        const response = await fetch('/api/candidates/queue?isBlind=' + isBlindMode);
        const queue = await response.json();

        document.getElementById('queueCount').innerText = queue.length;
        const queueList = document.getElementById('queueList');
        queueList.innerHTML = '';

        if (queue.length === 0) {
            queueList.innerHTML = '<li class="empty-msg">No candidates in queue</li>';
            return;
        }

        queue.forEach((candidate, index) => {
            const li = document.createElement('li');
            li.className = 'queue-item';

            if(index === 0) {
                li.style.borderLeft = "4px solid var(--secondary)";
                li.innerHTML = `<span>🥇 ${candidate.candidateAlias}</span> <span class="queue-score">${candidate.suitabilityScore}</span>`;
            } else {
                li.innerHTML = `<span>${index + 1}. ${candidate.candidateAlias}</span> <span class="queue-score">${candidate.suitabilityScore}</span>`;
            }
            queueList.appendChild(li);
        });
    } catch (error) {
        console.error("Error fetching queue:", error);
    }
}