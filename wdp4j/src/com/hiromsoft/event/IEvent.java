package com.hiromsoft.event;

public interface IEvent {
	public int getEventType();
	public Object getContext();
	public Object getSourceObject();
}
