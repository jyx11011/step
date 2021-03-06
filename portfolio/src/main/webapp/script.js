// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

let cursor = null;

google.charts.load('current', {'packages':['corechart']});
google.charts.setOnLoadCallback(drawCommentStatsChart);

/**
 * Adds a random fact to the page.
 */
function addRandomFact() {
  const facts =
      ['I\'m a CS student.', 'I love programming!', 'I love watching TV series.', 'I love cooking!'];
  const effects = 
      ['', 'shadow', 'shine', 'dark']

  // Pick a random fact.
  const fact = facts[Math.floor(Math.random() * facts.length)];

  // Pick a random effect.
  const effect = effects[Math.floor(Math.random() * effects.length)];

  // Add it to the page.
  const factContainer = document.getElementById('fact-container');
  factContainer.innerText = fact;
  factContainer.className = effect;
}

/**
 * Shuffles an array in place.
 */
function shuffle(arr) {
  arr.sort((a, b) => Math.random() - 0.5);
}

/**
 * Shuffles images in the gallery.
 */
function shuffleImagesInGallery() {
  const gallery = document.getElementById('gallery');
  const images = gallery.getElementsByTagName('img');
  shuffle(Array.from(images));
  for (const image of images) {
    gallery.appendChild(image);
  }
}

/**
 * Returns the comment limit value.
 */
function getCommentLimit() {
  const limitInput = document.getElementById('comment-limit').value;
  return limitInput ? limitInput : undefined;
}

/**
 * Returns the comments user email filter.
 */
function getUserEmailFilter() {
  const userEmail = document.getElementById('user-email-filter').value;
  return userEmail ? userEmail : undefined;
}

/**
 * Returns the comments sort order.
 */
function getCommentsSortOrder() {
  const order = document.getElementById('comment-order').value;
  return order;
}

/**
 * Fetches comments that satisfy user's requirements.
 */
function fetchCommentsWithUserInput() {
  const limit = getCommentLimit();
  const userEmailFilter = getUserEmailFilter();
  const sortOrder = getCommentsSortOrder();
  const map = new Map([
      ['limit', limit], ['user-email', userEmailFilter], ['order', sortOrder]]);
  fetchComments(map);
  return false;
}

/**
 * Fetches next batch of comments.
 */
function fetchNextPageOfComments() {
  const limit = getCommentLimit();
  const userEmailFilter = getUserEmailFilter();
  const sortOrder = getCommentsSortOrder();
  const map = new Map([
      ['limit', limit], ['user-email', userEmailFilter], ['order', sortOrder], ['start', cursor]]);
  fetchComments(map, false);
}

/**
 * Fetches comments with the given requirements.
 */
function fetchComments(requirements, removeExisting = true) {
  fetch('/comments' + getRequestParameter(requirements))
    .then(response => {
      if (!response.ok) {
        throw new Error(response.statusText);
      }
      return response.json()
    }).then(json => {
      const comments = json.comments;
      const commentsContainer = document.getElementById('comments-container');
      if (removeExisting) {
        commentsContainer.innerHTML = '';
      }

      if (json.isEndOfComments) {
        toggleLoadMoreButton(false);
        return;
      }

      toggleLoadMoreButton(true);
      for (const comment of comments) {
        const commentElement = createElementForComment(comment);
        commentsContainer.appendChild(commentElement);
      }

      cursor = json.cursor;
    }).catch(error => console.log(error));
}

function toggleLoadMoreButton(visible) {
  const noMoreCommentsContainer = document.getElementById('no-more-comments');
  const loadMoreButton = document.getElementById('load-more-btn');
  if (visible) {
    loadMoreButton.style.display = 'inline-block';
    noMoreCommentsContainer.className = 'hide';
  } else {
    loadMoreButton.style.display = 'none';
    noMoreCommentsContainer.className = 'appear';
  }
}

/**
 * Returns a request string for the given key,value paris.
 */
function getRequestParameter(map) {
  if (map == null) {
    return '';
  }
  // Ignore entries whose value is undefined
  Array.from(map.keys())
    .filter(key => isUndefined(map.get(key)))
    .forEach(key => map.delete(key));

  const params = new URLSearchParams(Object.fromEntries(map));
  return '?' + params.toString();
}

function isUndefined(value) {
  return typeof value == 'undefined'
}

function resetComments() {
  fetchComments();
}

function createCommentHeader(comment) {
  const header = document.createElement('div');
  header.className = 'comment-header';
  const usernameElement = document.createElement('span');
  usernameElement.innerText = comment.userEmail;
  usernameElement.className = 'username';

  fetchNicknameOfUser(comment.userEmail, 
      (nickname) => {
        usernameElement.innerText = nickname
      });
  const timestampElement = document.createElement('span');
  timestampElement.innerText = (new Date(parseInt(comment.timestamp))).toLocaleString();
  timestampElement.className = 'timestamp';
  header.append(usernameElement, timestampElement);
  return header;
}

function createCommentButton(comment) {
  const commentButtonElement = document.createElement('div');
  commentButtonElement.className = 'comment-button';
  const deleteButton = document.createElement('button');
  deleteButton.innerText = 'Delete';
  deleteButton.addEventListener('click', () => deleteComment(comment.id));
  deleteButton.className = 'delete-comment';
  commentButtonElement.append(deleteButton);

  return commentButtonElement;
}

function createCommentContent(comment) {
  const contentElement = document.createElement('div');
  contentElement.innerText = comment.content;
  contentElement.className = 'content';

  if (comment.imageUrl) {
    const imageElement = document.createElement('img');
    imageElement.src = comment.imageUrl;
    imageElement.alt = 'comment image';
    contentElement.append(imageElement);
  }

  return contentElement;
}

function createElementForComment(comment) {
  const commentElement = document.createElement('div');
  commentElement.className = 'comment'

  const header = createCommentHeader(comment);
  const content = createCommentContent(comment);
  const button = createCommentButton(comment);
  
  commentElement.append(header, content, button);
  return commentElement;
}

function fetchNicknameOfUser(email, completionHandler) {
  fetch('/nickname?email=' + email)
    .then(response => response.json())
    .then(json => {
      if (json.nickname == null) {
        return;
      }
      completionHandler(json.nickname);
    });
}

function deleteAllComments() {
  const request = new Request('/comments', { method: 'DELETE' });
  fetch(request).then(_ => {
    fetchComments();
    drawCommentStatsChart();
   });
}

function deleteComment(id) {
  const request = new Request('/comments?id=' + id, { method: 'DELETE' });
  fetch(request).then(response => {
    fetchComments();
    drawCommentStatsChart();
   });
}

/**
 * Configures comment form depending on user login status;
 */
function configureCommentForm() {
  fetch("/user")
  .then(response => response.json())
  .then(loginStatus => {
    if (loginStatus.isLoggedIn) {
      showCommentForm(loginStatus);
      showNicknameForm();
    } else {
      hideCommentForm(loginStatus);
      hideNicknameForm();
    }
  })
}

function hideCommentForm(loginStatus) {
  const loginLink = document.getElementById('login-link');
  loginLink.href = loginStatus.loginUrl;
  const logoutContainer = document.getElementById('logout-container');
  logoutContainer.hidden = true;
  const commentForm = document.getElementById('comment-form');
  commentForm.classList.add('logout')
}

function showCommentForm(loginStatus) {
  const loginContainer = document.getElementById('login-container');
  loginContainer.hidden = true;
  const logoutContainer = document.getElementById('logout-container');
  logoutContainer.hidden = false;
  const username = document.getElementById('username');
  username.innerText = loginStatus.nickname ? loginStatus.nickname : loginStatus.userEmail;
  const logoutLink = document.getElementById('logout-link');
  logoutLink.href = loginStatus.logoutUrl;
  const commentForm = document.getElementById('comment-form');
  commentForm.classList.remove('logout');
}

function showNicknameForm() {
  const nicknameForm = document.getElementById('nickname-form');
  nicknameForm.hidden = false;
}

function hideNicknameForm() {
  const nicknameForm = document.getElementById('nickname-form');
  nicknameForm.hidden = true;
}

function addBlobstoreUrlToForm() {
  fetch('/blobstore-upload-url')
    .then((response) => {
      return response.text();
    })
    .then((imageUploadUrl) => {
      const commentForm = document.getElementById('comment-form');
      commentForm.action = imageUploadUrl;
      commentForm.classList.remove('hide');
    });
}

/** Creates a chart for comments and adds it to the page. */
function drawCommentStatsChart() {
  let data = new google.visualization.DataTable();
  data.addColumn('string', 'Date');
  data.addColumn('number', 'Number of comments');
  const options = {
     title: 'Comments statistics over the last 7 days',
     height: 400,
     chartArea: { width:'80%', height:'75%' },
     vAxis: {
        title: 'Number of comments'
      },
      hAxis: {
        title: 'Date'
      },
      legend: 'bottom'
  };
  const chart = new google.visualization.LineChart(document.getElementById('comment-stats-chart'));

  const today = new Date();
  const dates = datesInWeekBefore(today).map(date => formatDate(date));
  const params = 'start-date=' + dates[0] + '&end-date=' + dates[6];
  fetch('/comments-stats?' + params)
    .then(response => response.json())
    .then(commentsCount => {
      for (const date of dates) {
        let numberOfComment = commentsCount[date];
        if (numberOfComment == null) {
          numberOfComment = 0;
        }
        data.addRow([date, numberOfComment]);
      }
      chart.draw(data, options);
    });
}

/** Returns dates in the week before the given date. */
function datesInWeekBefore(date) {
  let dates = [];
  for (i = -6; i <= 0; i++) {
    const currentDate = new Date();
    currentDate.setDate(date.getDate() + i);
    dates.push(currentDate);
  }
  return dates;
}

/** Formats date into YYYY-MM-DD. */
function formatDate(date) {
  return date.getFullYear() + '-' 
      + standardize(date.getMonth() + 1, 2) + '-' 
      + standardize(date.getDate(), 2);
}

/** 
 * Returns a string representation of the number after 
 * standardizing it to the required number of digits. 
*/
function standardize(number, digit){
  if (number.toString().length < digit) {
    return '0'.repeat(digit - number.toString().length) + number;
  }
  return number.toString();
}

window.onload = () => {
  fetchComments();
  addRandomFact();
  configureCommentForm();
  addBlobstoreUrlToForm();
}
