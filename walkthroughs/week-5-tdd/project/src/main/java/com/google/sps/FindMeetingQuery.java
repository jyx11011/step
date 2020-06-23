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

package com.google.sps;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.stream.Collectors;
import java.util.List;

public final class FindMeetingQuery {

  /**
   * A comparator for sorting events by their start time in ascending order.
   */
  private static Comparator<Event> eventsComparator = 
          Comparator.comparing(Event::getWhen, TimeRange.ORDER_BY_START);

  /**
   * Returns true if the given attendees and attendees in the event overlap.
   */
  private boolean isAttendeesOverlapping(Event event, Collection<String> attendees) {
    return !Collections.disjoint(event.getAttendees(), attendees);
  }

  /**
   * Returns a list of sorted events that overlap with the meeting request. Whether or not
   * optional attendees are considered depends on the includesOptional.
   */
  private List<Event> sortOverlappingEvents(Collection<Event> events, MeetingRequest request, 
      boolean includesOptional) {
    List<Event> eventsList = events.stream()
        .filter(event -> {
          if (includesOptional) {
            return isAttendeesOverlapping(event, request.getAttendees()) ||
                isAttendeesOverlapping(event, request.getOptionalAttendees());
          } else {
            return isAttendeesOverlapping(event, request.getAttendees());
          }
        })
        .collect(Collectors.toList());

    Collections.sort(eventsList, eventsComparator);
    return eventsList;
  }

  /**
   * Returns a list of time ranges fit the meeting request given the sorted list events that
   * overlap with the request.
   */
  private Collection<TimeRange> query(List<Event> eventsList, MeetingRequest request) {
    List<TimeRange> availableTimeRanges = new ArrayList<>();
    int nextFreeMinute = TimeRange.START_OF_DAY;
    for(Event event: eventsList) {
      if (event.getWhen().start() > nextFreeMinute) {
        // Forms a new time range.
        TimeRange availableTimeRange = TimeRange.fromStartEnd(nextFreeMinute, event.getWhen().start(), false);
        if (availableTimeRange.duration() >= request.getDuration()) {
          availableTimeRanges.add(availableTimeRange);
        }
        nextFreeMinute = event.getWhen().end();
      } else {
        // The event either is included in the occupied time range or extends the occupied time range.
        if (event.getWhen().end() > nextFreeMinute) {
          nextFreeMinute = event.getWhen().end();
        }
      }
    }

    if (nextFreeMinute < TimeRange.END_OF_DAY) {
      TimeRange availableTimeRange = TimeRange.fromStartEnd(nextFreeMinute, TimeRange.END_OF_DAY, true);
      if (availableTimeRange.duration() >= request.getDuration()) {
        availableTimeRanges.add(availableTimeRange);
      }
    }

    return availableTimeRanges;
  }

  /**
   * Returns a list of time ranges fit the meeting request.
   */
  public Collection<TimeRange> query(Collection<Event> events, MeetingRequest request) {
    // Queries time ranges for both mandatory and optional attendees.
    List<Event> eventsList = sortOverlappingEvents(events, request, true);
    Collection<TimeRange> results = query(eventsList, request);

    if (results.isEmpty() && !request.getAttendees().isEmpty()) {
      // Queries time ranges for only mandatory attendees.
      eventsList = sortOverlappingEvents(events, request, false);
      results = query(eventsList, request);
    }
    return results;
  }
}
