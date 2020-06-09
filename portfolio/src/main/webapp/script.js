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
 * Returns the comments username filter.
 */
function getUsernameFilter() {
  const username = document.getElementById('username-filter').value;
  return username ? username : undefined;
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
  const usernameFilter = getUsernameFilter();
  const sortOrder = getCommentsSortOrder();
  const map = new Map([
      ['limit', limit], ['username', usernameFilter], ['order', sortOrder]]);
  fetchComments(map);
  return false;
}

/**
 * Fetches next batch of comments.
 */
function fetchNextPageOfComments() {
  const limit = getCommentLimit();
  const usernameFilter = getUsernameFilter();
  const sortOrder = getCommentsSortOrder();
  const map = new Map([
      ['limit', limit], ['username', usernameFilter], ['order', sortOrder], ['start', cursor]]);
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
  if (isUndefined(map)) {
    return '';
  }

  let results = '';
  let first = true;
  for(const [key, value] of map.entries()) {
    if (isUndefined(value)) {
      continue;
    }
    if (first) {
      results = '?';
      first = false;
    } else {
      results += '&';
    }
    results += key + '=' + value;
  }
  return results;
}

function isUndefined(value) {
  return typeof value == 'undefined'
}

function resetComments() {
  fetchComments();
}

function createElementForComment(comment) {
  const commentElement = document.createElement('div');
  commentElement.className = 'comment'
  
  const header = document.createElement('div');
  header.className = 'comment-header';
  const usernameElement = document.createElement('span');
  usernameElement.innerText = comment.username;
  usernameElement.className = 'username';
  
  const timestampElement = document.createElement('span');
  timestampElement.innerText = (new Date(parseInt(comment.timestamp))).toLocaleString();
  timestampElement.className = 'timestamp';
  header.append(usernameElement, timestampElement)
  
  const contentElement = document.createElement('div');
  contentElement.innerText = comment.content;
  contentElement.className = 'content';
  
  const commentButtonElement = document.createElement('div');
  commentButtonElement.className = 'comment-button';
  const deleteButton = document.createElement('button');
  deleteButton.innerText = 'Delete';
  deleteButton.addEventListener('click', () => deleteComment(comment.id));
  deleteButton.className = 'delete-comment';
  commentButtonElement.append(deleteButton);

  commentElement.append(header, contentElement, commentButtonElement);
  return commentElement;
}

function deleteAllComments() {
  const request = new Request('/comments', { method: 'DELETE' });
  fetch(request).then(_ => fetchComments());
}

function deleteComment(id) {
  const request = new Request('/comments?id=' + id, { method: 'DELETE' });
  fetch(request).then(response => fetchComments());
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
  commentForm.className = 'hide';
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
  commentForm.className = '';
}

function showNicknameForm() {
  const nicknameForm = document.getElementById('nickname-form');
  nicknameForm.hidden = false;
}

function hideNicknameForm() {
  const nicknameForm = document.getElementById('nickname-form');
  nicknameForm.hidden = true;
}

window.onload = () => {
  fetchComments();
  addRandomFact();
  configureCommentForm();
}
