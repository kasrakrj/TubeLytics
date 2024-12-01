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
        var data = JSON.parse(event.data);

        if (data.type === 'video') {
            var keyword = data.keyword;
            var video = data;
            var safeKey = keyword.replace(/[^a-zA-Z0-9]/g, '_');

            var videoList = document.getElementById('video-list-' + safeKey);

            if (!videoList) {
                createKeywordSection(keyword, safeKey);
                videoList = document.getElementById('video-list-' + safeKey);
            }

            var li = document.createElement('li');
            li.classList.add('video-item');
            li.style.backgroundColor = '#f0bdbd';

            var img = document.createElement('img');
            img.src = video.thumbnailUrl;
            img.alt = 'Thumbnail';

            var div = document.createElement('div');

            var h3 = document.createElement('h3');
            var a = document.createElement('a');
            a.href = 'https://www.youtube.com/watch?v=' + video.videoId;
            a.target = '_blank';
            a.textContent = video.title;
            h3.appendChild(a);

            var p = document.createElement('p');
            p.textContent = video.description;

            var small = document.createElement('small');
            small.innerHTML = 'Channel: <a href="/channel/' + video.channelId + '" target="_blank">' + video.channelTitle + '</a>';

            var tagsLink = document.createElement('p');
            var tagsA = document.createElement('a');
            tagsA.href = '/tags/' + video.videoId;
            tagsA.target = '_blank';
            tagsA.textContent = 'Tags';
            tagsLink.appendChild(tagsA);

            div.appendChild(h3);
            div.appendChild(p);
            div.appendChild(small);
            div.appendChild(tagsLink);

            li.appendChild(img);
            li.appendChild(div);

            videoList.insertBefore(li, videoList.firstChild);
            while (videoList.children.length > 10) {
                            // Remove the last child (oldest video)
                            videoList.removeChild(videoList.lastChild);
                        }
        } else if (data.type === 'sentiment') {

            var keyword = data.keyword;
            var sentiment = data.sentiment;
            var safeKey = keyword.replace(/[^a-zA-Z0-9]/g, '_');

            var sentimentElement = document.getElementById('sentiment-' + safeKey);
            if (sentimentElement) {
                sentimentElement.textContent = 'Sentiment for \'' + keyword + '\': ' + sentiment;
            }
        } else if (data.type === 'heartbeat') {
            console.log('Received heartbeat from server.');
        }
    });

    function createKeywordSection(keyword, safeKey) {
        var h2 = document.createElement('h2');
        h2.textContent = "Search Results for '" + keyword + "'";
        document.body.appendChild(h2);

        var sentimentDiv = document.createElement('div');
        sentimentDiv.className = 'sentiment';
        sentimentDiv.id = 'sentiment-' + safeKey;
        sentimentDiv.textContent = "Sentiment for '" + keyword + "': N/A";
        document.body.appendChild(sentimentDiv);

        var statsDiv = document.createElement('div');
        var statsLink = document.createElement('a');
        statsLink.href = '/wordStats/' + encodeURIComponent(keyword);
        statsLink.target = '_blank';
        statsLink.textContent = 'Search Stats';
        statsDiv.appendChild(statsLink);
        document.body.appendChild(statsDiv);

        var ul = document.createElement('ul');
        ul.className = 'video-list';
        ul.id = 'video-list-' + safeKey;
        document.body.appendChild(ul);

        var hr = document.createElement('hr');
        document.body.appendChild(hr);
    }
