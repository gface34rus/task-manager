const API_URL = '/api/tasks';
let tasks = [];
let currentFilter = 'ALL';
let searchTerm = ''; // Search state

// DOM Elements
const taskList = document.getElementById('taskList');
const addTaskBtn = document.getElementById('addTaskBtn');
const taskTitleInput = document.getElementById('taskTitle');
const taskDueDateInput = document.getElementById('taskDueDate'); // New input
const titleError = document.getElementById('titleError');
const filterBtns = document.querySelectorAll('.filter-btn');
const searchInput = document.getElementById('searchInput'); // New input
const modal = document.getElementById('taskModal');
const closeModal = document.querySelector('.close-modal');
const saveTaskBtn = document.getElementById('saveTaskBtn');

// Modal inputs
const editTaskId = document.getElementById('editTaskId');
const editTaskTitle = document.getElementById('editTaskTitle');
const editTaskDesc = document.getElementById('editTaskDesc');
const editTaskDueDate = document.getElementById('editTaskDueDate'); // New input
const editTaskStatus = document.getElementById('editTaskStatus');

// Initialize
document.addEventListener('DOMContentLoaded', fetchTasks);

// Event Listeners
addTaskBtn.addEventListener('click', createTask);
taskTitleInput.addEventListener('keypress', (e) => {
    if (e.key === 'Enter') createTask();
});

// Search Listener
searchInput.addEventListener('input', (e) => {
    searchTerm = e.target.value.toLowerCase();
    renderTasks();
});

filterBtns.forEach(btn => {
    btn.addEventListener('click', () => {
        filterBtns.forEach(b => b.classList.remove('active'));
        btn.classList.add('active');
        currentFilter = btn.dataset.filter;
        renderTasks();
    });
});

closeModal.onclick = () => modal.classList.remove('show');
window.onclick = (e) => {
    if (e.target == modal) modal.classList.remove('show');
}
saveTaskBtn.onclick = updateTask;

// API Functions
async function fetchTasks() {
    try {
        const response = await fetch(API_URL);

        if (response.status === 401) {
            window.location.href = '/login.html';
            return;
        }

        tasks = await response.json();
        renderTasks();
        checkUser(); // Check user info
    } catch (error) {
        console.error('Error fetching tasks:', error);
        taskList.innerHTML = '<div class="error-message">Ошибка загрузки задач</div>';
    }
}

async function checkUser() {
    try {
        const res = await fetch('/auth/user');
        if (res.ok) {
            const data = await res.json();
            const header = document.querySelector('header p');
            if (header) header.innerHTML = `Привет, <b><a href="/profile.html" style="color: inherit; text-decoration: underline;">${data.username}</a></b>! <a href="/logout" style="color: var(--danger-color); margin-left: 10px; text-decoration: none;">Выйти</a>`;
        }
    } catch (e) { }
}

async function createTask() {
    const title = taskTitleInput.value.trim();
    const dueDate = taskDueDateInput.value; // Get date
    if (!title) {
        showError(titleError, 'Введите название задачи');
        return;
    }
    showError(titleError, '');

    try {
        const response = await fetch(API_URL, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                title: title,
                description: '',
                status: 'PENDING',
                dueDate: dueDate || null // Send date
            })
        });

        if (response.ok) {
            taskTitleInput.value = '';
            taskDueDateInput.value = ''; // Clear date
            fetchTasks();
        } else {
            const err = await response.json();
            showError(titleError, err.title || 'Ошибка создания');
        }
    } catch (error) {
        console.error('Error creating task:', error);
    }
}

async function deleteTask(id, event) {
    event.stopPropagation();
    if (!confirm('Удалить эту задачу?')) return;

    try {
        await fetch(`${API_URL}/${id}`, { method: 'DELETE' });
        fetchTasks();
    } catch (error) {
        console.error('Error deleting task:', error);
    }
}

async function updateTask() {
    const id = editTaskId.value;
    const taskData = {
        title: editTaskTitle.value,
        description: editTaskDesc.value,
        status: editTaskStatus.value,
        dueDate: editTaskDueDate.value || null
    };

    try {
        const response = await fetch(`${API_URL}/${id}`, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(taskData)
        });

        if (response.ok) {
            modal.classList.remove('show');
            fetchTasks();
        } else {
            alert('Ошибка обновления');
        }
    } catch (error) {
        console.error('Error updating task:', error);
    }
}

// UI Functions
function renderTasks() {
    taskList.innerHTML = '';

    // Sort: PENDING first, then IN_PROGRESS, then COMPLETED. Within status by orderIndex
    let sortedTasks = [...tasks].sort((a, b) => {
        // First sort by orderIndex if exists (priority)
        if (a.orderIndex !== null && b.orderIndex !== null) {
            return a.orderIndex - b.orderIndex;
        }
        return b.id - a.id;
    });

    const filteredTasks = sortedTasks.filter(task => {
        // Filter by Status
        if (currentFilter !== 'ALL' && task.status !== currentFilter) return false;

        // Filter by Search Term
        if (searchTerm && !task.title.toLowerCase().includes(searchTerm)) return false;

        return true;
    });

    if (filteredTasks.length === 0) {
        taskList.innerHTML = '<div style="text-align:center; padding: 2rem; color: #9ca3af;">Ничего не найдено</div>';
        return;
    }

    filteredTasks.forEach(task => {
        const createdDate = new Date(task.createdAt).toLocaleDateString();

        // Due Date Logic
        let dueDateHtml = '';
        if (task.dueDate) {
            const due = new Date(task.dueDate);
            const isOverdue = due < new Date().setHours(0, 0, 0, 0) && task.status !== 'COMPLETED';
            dueDateHtml = `
                <span class="due-date ${isOverdue ? 'overdue' : ''}">
                    <i class="fa-regular fa-clock"></i> ${due.toLocaleDateString()}
                </span>
            `;
        }

        const card = document.createElement('div');
        card.className = `task-card status-${task.status}`;
        card.setAttribute('draggable', 'true'); // Make draggable
        card.dataset.id = task.id; // Store ID

        card.onclick = (e) => {
            if (!e.target.closest('.delete-btn')) openModal(task);
        };

        card.innerHTML = `
            <div class="task-content">
                <h3>${escapeHtml(task.title)}</h3>
                <div class="task-meta">
                    <span class="status-badge">${getStatusLabel(task.status)}</span>
                    ${dueDateHtml}
                </div>
            </div>
            <div class="task-actions">
                <button class="action-btn delete-btn" onclick="deleteTask(${task.id}, event)">
                    <i class="fa-solid fa-trash"></i>
                </button>
            </div>
        `;

        // Drag Events
        card.addEventListener('dragstart', () => {
            card.classList.add('dragging');
        });

        card.addEventListener('dragend', () => {
            card.classList.remove('dragging');
            saveOrder();
        });

        taskList.appendChild(card);
    });

    // Container Drag Events
    taskList.addEventListener('dragover', (e) => {
        e.preventDefault();
        const afterElement = getDragAfterElement(taskList, e.clientY);
        const draggable = document.querySelector('.dragging');
        if (afterElement == null) {
            taskList.appendChild(draggable);
        } else {
            taskList.insertBefore(draggable, afterElement);
        }
    });

    updateStats();
}

function updateStats() {
    const pending = tasks.filter(t => t.status === 'PENDING').length;
    const progress = tasks.filter(t => t.status === 'IN_PROGRESS').length;
    const completed = tasks.filter(t => t.status === 'COMPLETED').length;

    document.getElementById('statPending').textContent = pending;
    document.getElementById('statProgress').textContent = progress;
    document.getElementById('statCompleted').textContent = completed;
}

function getDragAfterElement(container, y) {
    const draggableElements = [...container.querySelectorAll('.task-card:not(.dragging)')];

    return draggableElements.reduce((closest, child) => {
        const box = child.getBoundingClientRect();
        const offset = y - box.top - box.height / 2;
        if (offset < 0 && offset > closest.offset) {
            return { offset: offset, element: child };
        } else {
            return closest;
        }
    }, { offset: Number.NEGATIVE_INFINITY }).element;
}

async function saveOrder() {
    const taskCards = [...taskList.querySelectorAll('.task-card')];
    const taskIds = taskCards.map(card => parseInt(card.dataset.id));

    try {
        await fetch(`${API_URL}/reorder`, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(taskIds)
        });
    } catch (error) {
        console.error('Error saving order:', error);
    }
}

function openModal(task) {
    editTaskId.value = task.id;
    editTaskTitle.value = task.title;
    editTaskDesc.value = task.description || '';
    editTaskStatus.value = task.status;
    editTaskDueDate.value = task.dueDate ? new Date(task.dueDate).toISOString().split('T')[0] : ''; // Fill date

    modal.classList.add('show');
}

function getStatusLabel(status) {
    const labels = {
        'PENDING': 'Ожидает',
        'IN_PROGRESS': 'В работе',
        'COMPLETED': 'Готово'
    };
    return labels[status] || status;
}

function showError(element, message) {
    element.textContent = message;
}

function escapeHtml(text) {
    if (!text) return '';
    return text
        .replace(/&/g, "&amp;")
        .replace(/</g, "&lt;")
        .replace(/>/g, "&gt;")
        .replace(/"/g, "&quot;")
        .replace(/'/g, "&#039;");
}
