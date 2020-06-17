function initMap() {
  const map = new google.maps.Map(document.getElementById('map-container'), {
    center: {lat: 1.352, lng: 103.8198},
    zoom: 12
  });

  const marker = new google.maps.Marker({
    map: map,
    animation: google.maps.Animation.DROP,
    position: {lat: 1.35, lng: 103.8198},
    label: 'A'
  });

  const infowindow = new google.maps.InfoWindow({
    content: 'Hello, world!'
  });

  marker.addListener('click', function() {
    infowindow.open(map, marker);
  });
}

window.onload = initMap;