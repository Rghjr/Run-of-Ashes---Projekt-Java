package com.runofashes;

import java.util.ArrayList;
import java.util.List;

public class EventsRegistry {
    private ArrayList<Event> events = new ArrayList<Event>();

    public List<Event> getEvents() {
        return events;
    }

    public void addEvent(Event event){
        events.add(event);
    }

    @Override
    public String toString() {
        return "EventsRegistry{" +
                "events=" + events +
                '}';
    }
}
