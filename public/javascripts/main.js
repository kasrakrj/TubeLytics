// Establish WebSocket connection
const socket = new WebSocket("ws://localhost:9000/ws");

// UI elements
const statusElement = document.getElementById("status");

// Assume 'searchKeywords' is provided in the HTML template as a JavaScript array

// Handle WebSocket connection open event
socket.addEventListener("open", (event) => {
    statusElement.innerText = "Connected to WebSocket!";
    // Send the keywords to the server via WebSocket
    if (searchKeywords && searchKeywords.length > 0) {
        const message = {
            type: "init",
            keywords: searchKeywords
        };
        socket.send(JSON.stringify(message));
    }
});

// Handle WebSocket connection close event
socket.addEventListener("close", (event) => {
    statusElement.innerText = "WebSocket connection closed.";
});

// Handle incoming WebSocket messages (new videos or heartbeat)
socket.addEventListener("message", (event) => {
    let data;
    try {
        data = JSON.parse(event.data);
        console.log(data);
    } catch (e) {
        console.error("Failed to parse message:", event.data);
        return;
    }

    if (data.type === "heartbeat") {
        // This is a heartbeat message
        console.log("Received heartbeat");
        return;
    }

    if (data.type === "video") {
        // Handle video data
        const videoData = data;

        // Generate the id of the corresponding video-list
        const safeKeyword = videoData.keyword.replace(/[^a-zA-Z0-9]/g, '_');
        const listId = 'video-list-' + safeKeyword;

        // Find the corresponding list element
        const videoListElement = document.getElementById(listId);

        if (videoListElement) {
            // Create a new list item for the incoming video
            const videoItem = document.createElement("li");
            videoItem.className = "video-item new-video-item"; // Add the new class here if needed

            videoItem.innerHTML = `
                <img src="${videoData.thumbnailUrl}" alt="Thumbnail">
                <div>
                    <h3>
                        <a href="https://www.youtube.com/watch?v=${videoData.videoId}" target="_blank">${videoData.title}</a>
                    </h3>
                    <p>${videoData.description}</p>
                    <small>
                        Channel:
                        <a href="/channel/${videoData.channelId}" target="_blank">${videoData.channelTitle}</a>
                    </small>
                    <p><a href="/tags/${videoData.videoId}" target="_blank">Tags</a></p>
                </div>
            `;

            // Insert the new video item at the beginning of the list
            videoListElement.insertBefore(videoItem, videoListElement.firstChild);

            // Now, check if the total number of videos exceeds 10
            while (videoListElement.children.length > 10) {
                // Remove the last child (oldest video)
                videoListElement.removeChild(videoListElement.lastChild);
            }
        } else {
            console.warn(`No videos section found for keyword: ${videoData.keyword}`);
        }
    } else {
        console.warn("Unknown message type:", data.type);
    }
});