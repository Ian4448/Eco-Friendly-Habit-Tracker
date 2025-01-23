// FAQ Data
const faqData = {
    "What is the Eco-Friendly Habit Tracker?":
        "The Eco-Friendly Habit Tracker is an open-source project designed to help individuals monitor and reduce their carbon footprint through personalized recommendations and habit tracking. You can explore the source code on GitHub: https://github.com/Ian4448/Eco-Friendly-Habit-Tracker.",
    "What technologies are used in this project?":
        "The project is built using a Spring Boot backend, H2 database for development, REST APIs, HTMX for dynamic frontend updates, and Google Maps API for route suggestions. The frontend is crafted with HTML, CSS, and JavaScript for a clean and responsive user experience.",
    "What features does the project currently offer?":
        "Current features include carbon footprint tracking, eco-friendly route suggestions based on Google Maps API, a user-friendly habit tracker, daily action tracking, goal setting, and streak monitoring. It also includes secure user authentication with password encryption using BCrypt.",
    "Is this project open-source?":
        "Yes, the Eco-Friendly Habit Tracker is completely open-source! Developers are encouraged to contribute, suggest improvements, or fork the project. Check it out on GitHub: https://github.com/Ian4448/Eco-Friendly-Habit-Tracker.",
    "Why was this project created?":
        "I made this project for fun because I'm passionate about eco-friendly tech and sustainability. It's been a great way to combine my interests with programming, and I'm always happy to chat with anyone interested in this space. Feel free to reach out!",
    "What are the future plans for the project?":
        "Future plans include integrating detailed analytics, adding support for business users, enhancing the user interface with advanced visualizations, and expanding habit tracking capabilities to include a wider range of eco-friendly activities.",
    "How can I contribute to the project?":
        "Contributions are welcome! Whether it's fixing bugs, adding features, or improving documentation, feel free to make a pull request or open an issue on the GitHub repository: https://github.com/Ian4448/Eco-Friendly-Habit-Tracker."
};

// DOM Elements
const chatButton = document.getElementById('chatButton');
const chatContainer = document.getElementById('chatContainer');
const chatClose = document.getElementById('chatClose');
const chatMessages = document.getElementById('chatMessages');

// Floating elements animation
function createFloatingElements() {
    const container = document.getElementById('floatingElements');
    const numElements = 5;

    for (let i = 0; i < numElements; i++) {
        const element = document.createElement('div');
        element.className = 'float-element';
        element.style.width = `${Math.random() * 100 + 50}px`;
        element.style.height = '150px';
        element.style.left = `${Math.random() * 100}%`;
        element.style.top = `${Math.random() * 100}%`;

        container.appendChild(element);

        gsap.to(element, {
            y: `${Math.random() * 100 - 50}`,
            x: `${Math.random() * 100 - 50}`,
            duration: Math.random() * 3 + 2,
            repeat: -1,
            yoyo: true,
            ease: "sine.inOut"
        });
    }
}

// Chat Functions
function showTypingIndicator() {
    const indicator = document.createElement('div');
    indicator.className = 'typing-indicator';
    indicator.innerHTML = `
        <div class="typing-dot"></div>
        <div class="typing-dot"></div>
        <div class="typing-dot"></div>
    `;
    chatMessages.appendChild(indicator);
    chatMessages.scrollTop = chatMessages.scrollHeight;
    return indicator;
}

function removeTypingIndicator(indicator) {
    indicator.remove();
}

function addMessage(text, type) {
    const message = document.createElement('div');
    message.className = `message ${type}-message`;
    message.textContent = text;
    chatMessages.appendChild(message);
    chatMessages.scrollTop = chatMessages.scrollHeight;
}

function addOptions() {
    const optionsContainer = document.createElement('div');
    optionsContainer.className = 'options-message';

    Object.keys(faqData).forEach(question => {
        const button = document.createElement('button');
        button.className = 'chat-option';
        button.textContent = question;
        button.onclick = () => handleQuestion(question);
        optionsContainer.appendChild(button);
    });

    chatMessages.appendChild(optionsContainer);
    chatMessages.scrollTop = chatMessages.scrollHeight;
}

function addConfirmationButtons() {
    const buttonsContainer = document.createElement('div');
    buttonsContainer.className = 'options-message';

    const confirmationHTML = `
        <p>Would you like to know anything else?</p>
        <div class="confirmation-buttons">
            <button class="confirm-button yes-button">Yes</button>
            <button class="confirm-button no-button">No</button>
        </div>
    `;

    buttonsContainer.innerHTML = confirmationHTML;
    chatMessages.appendChild(buttonsContainer);
    chatMessages.scrollTop = chatMessages.scrollHeight;

    // Add event listeners
    const yesButton = buttonsContainer.querySelector('.yes-button');
    const noButton = buttonsContainer.querySelector('.no-button');

    yesButton.onclick = () => {
        buttonsContainer.remove();
        const typingIndicator = showTypingIndicator();
        setTimeout(() => {
            removeTypingIndicator(typingIndicator);
            addOptions();
        }, 1500);
    };

    noButton.onclick = () => {
        buttonsContainer.remove();
        const typingIndicator = showTypingIndicator();
        setTimeout(() => {
            removeTypingIndicator(typingIndicator);
            showFarewellMessage();
        }, 1500);
    };
}

function handleQuestion(question) {
    // Add user's question
    addMessage(question, 'user');

    // Show typing indicator
    const typingIndicator = showTypingIndicator();

    // Remove typing indicator and show response after delay
    setTimeout(() => {
        removeTypingIndicator(typingIndicator);
        addMessage(faqData[question], 'bot');

        // Show typing indicator before confirmation
        const confirmTypingIndicator = showTypingIndicator();

        // Remove typing indicator and show confirmation after delay
        setTimeout(() => {
            removeTypingIndicator(confirmTypingIndicator);
            addConfirmationButtons();
        }, 3000);
    }, 1500);
}

function showFarewellMessage() {
    const farewell = document.createElement('div');
    farewell.className = 'message bot-message farewell-message';
    farewell.textContent = 'Thank you for chatting! Have a great day! 👋';
    chatMessages.appendChild(farewell);
    chatMessages.scrollTop = chatMessages.scrollHeight;

    // Close chat after delay
    setTimeout(() => {
        chatContainer.style.display = 'none';
        chatButton.style.display = 'flex';

        // Clear chat history after chat is closed
        setTimeout(() => {
            chatMessages.innerHTML = '';
            initChat();
        }, 1000);
    }, 2000);
}

// Start new chat sequence
function startChat() {
    const typingIndicator = showTypingIndicator();

    setTimeout(() => {
        removeTypingIndicator(typingIndicator);
        addMessage("Hi! I'm here to help you learn more about EcoTrack. What would you like to know?", 'bot');

        const optionsTypingIndicator = showTypingIndicator();
        setTimeout(() => {
            removeTypingIndicator(optionsTypingIndicator);
            addOptions();
        }, 2000);
    }, 1500);
}

// Initialize chat (for first load and after closing)
function initChat() {
    chatMessages.innerHTML = '';
    startChat();
}

// Event listeners
chatButton.addEventListener('click', () => {
    chatContainer.style.display = 'flex';
    chatButton.style.display = 'none';
    chatMessages.innerHTML = '';
    startChat();
});

chatClose.addEventListener('click', () => {
    chatContainer.style.display = 'none';
    chatButton.style.display = 'flex';
});

// Prevent scroll propagation from chat messages to the main window
chatMessages.addEventListener('wheel', function(e) {
    const scrollPosition = chatMessages.scrollTop;
    const scrollHeight = chatMessages.scrollHeight;
    const containerHeight = chatMessages.clientHeight;

    // Check if we're at the top or bottom of the scroll
    if ((scrollPosition <= 0 && e.deltaY < 0) ||
        (scrollPosition + containerHeight >= scrollHeight && e.deltaY > 0)) {
        // Allow scrolling of main page only at boundaries
        return;
    }

    e.stopPropagation();
    e.preventDefault();

    // Manual scroll
    chatMessages.scrollTop += e.deltaY;
}, { passive: false });

// Initialize floating elements when the page loads
createFloatingElements();