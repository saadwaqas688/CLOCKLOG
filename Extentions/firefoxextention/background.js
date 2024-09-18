// let currentUrl = null;

// Function to get the current active tab's URL and send it
function fetchAndSendUrl() {
  browser.tabs.query({ active: true, currentWindow: true }).then((tabs) => {
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
          body: JSON.stringify({ url: url, browser: 'firefox' }),
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
