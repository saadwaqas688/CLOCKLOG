// let currentUrl = null;

// // Detect when the active tab changes
// chrome.tabs.onActivated.addListener((activeInfo) => {
//     chrome.tabs.get(activeInfo.tabId, (tab) => {
//         updateCurrentUrl(tab.url);
//     });
// });

// // Detect when a tab is updated (e.g., page load complete)
// chrome.tabs.onUpdated.addListener((tabId, changeInfo, tab) => {
//     if (changeInfo.status === 'complete') {
//         updateCurrentUrl(tab.url);
//     }
// });

// // Function to update the URL and send it if necessary
// function updateCurrentUrl(url) {
//     if (url) {
//         currentUrl = url;
//         sendUrlToJavaApp(currentUrl);  // Send the URL immediately when it changes
//     }
// }

// // Function to send the URL to the Java app
// function sendUrlToJavaApp(url) {
//     if (url) {
//         fetch('http://localhost:5025/receive-url', {
//             method: 'POST',
//             headers: {
//                 'Content-Type': 'application/json',
//             },
//             body: JSON.stringify({ url: url ,browser:'chrome'}),
//         })
//         .then((response) => response.json())
//         .then((data) => console.log('Success:', data))
//         .catch((error) => console.error('Error:', error));
//     }
// }

// // Send the URL every 10 seconds
// setInterval(() => {
//     if (currentUrl) {
//         sendUrlToJavaApp(currentUrl);  // Send the current URL every 10 seconds
//     }
// }, 10000);  // 10 seconds in milliseconds
let currentUrl = null;

// Function to get the current active tab's URL and send it
function fetchAndSendUrl() {
    chrome.tabs.query({ active: true, currentWindow: true }, (tabs) => {
        const tab = tabs[0];
        if (tab && tab.url) {
            currentUrl = tab.url;
            sendUrlToJavaApp(currentUrl);  // Send the URL every 10 seconds
        }
    });
}

// Function to send the URL to the Java app
function sendUrlToJavaApp(url) {
    if (url) {
        fetch('http://localhost:5025/receive-url', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({ url: url, browser: 'chrome' }),
        })
        .then((response) => response.json())
        .then((data) => console.log('Success:', data))
        .catch((error) => console.error('Error:', error));
    }
}

// Fetch and send the URL every 10 seconds
function startPeriodicUrlSending() {
    fetchAndSendUrl();  // Initial fetch and send
    setInterval(fetchAndSendUrl, 10000);  // 10 seconds interval
}

// Start the periodic URL sending when the extension is loaded
startPeriodicUrlSending();
