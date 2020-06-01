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
  for (var image of images) {
    gallery.appendChild(image);
  }
}

/**
 * Adds a greeting message from the servlet to the page.
 */
function addAGreetingMessage() {
  fetch('/data')
    .then(response => response.text())
    .then(message => {
      const greetingContainer = document.getElementById('greeting-container');
      greetingContainer.innerHTML = message;
    });
}
