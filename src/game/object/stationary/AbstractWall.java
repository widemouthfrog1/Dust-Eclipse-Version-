package game.object.stationary;
import java.util.ArrayList;

import game.object.entity.Entity;
import logic.DVector;
import logic.Vector;
import processing.core.*;

public abstract class AbstractWall implements Wall {
	ArrayList<Vector> points = new ArrayList<Vector>();
	
	public AbstractWall(ArrayList<Vector> points){
		this.points = points;
	}
	
	AbstractWall(float x1, float y1, float x2, float y2){
		this.points.add(new DVector(x1, y1));
		this.points.add(new DVector(x2, y2));
	}
	
	/**
	 * {@inheritDoc}
	 * 
	 * Default points, returns an ArrayList containing copies of the first and last point of the wall
	 */
	@Override
	public ArrayList<Vector> points() {
		if(points.size() < 2) {
			return null;
		}
		ArrayList<Vector> definingPoints = new ArrayList<Vector>();
		definingPoints.add(new DVector(this.points.get(0).x(), this.points.get(0).y()));
		definingPoints.add(new DVector(this.points.get(this.points.size()-1).x(), this.points.get(this.points.size()-1).y()));
		return definingPoints;
	}

	@Override
	public void move(Vector amount) {
		for(Vector point : this.points) {
			point.add(amount);
		}
	}
	
	/**
	 * {@inheritDoc}
	 * 
	 * Default setPosition, moves the first point in the wall to the position (and all other points relatively)
	 */
	@Override
	public void setPosition(Vector position) {
		for(Vector point : this.points) {
			point.add(Vector.sub(this.points.get(0), position));
		}
	}

	@Override
	public void draw(PApplet app, Vector offset) {
		app.stroke(255);
		app.strokeWeight(3);
		app.line(this.points().get(0).x() + offset.x() + app.width/2, this.points().get(0).y()+ offset.y() + app.height/2, this.points().get(this.points.size()-1).x() + offset.x() + app.width/2, this.points().get(this.points.size()-1).y()+ offset.y() + app.height/2);
	}
	
	@Override
	public boolean getPlayerSide(Vector absolutePosition) {  	
		return isPointInFront(absolutePosition);//if wall.a*player.x + wall.b*player.y + wall.c > 0, player counts as being in front of wall, otherwise, player is behind wall
	}
	
	@Override
	public boolean inBounds(Vector position, Vector velocity) {
		return logic.Math.intersectionOnSegment(position, Vector.add(position, velocity),this.points().get(0), this.points().get(1), false);
	}
	
	/**
	 * Returns the intersection of a line and the line create by this wall, given 2 points
	 * @param position
	 * @param velocity
	 */
	public Vector getIntersection(Vector position, Vector velocity) {//was position and position+velocity
		return logic.Math.intersection(position, Vector.add(position, velocity), this.points().get(0), this.points().get(1));
	}
	
	public Vector getNormal(Entity e) {
		Vector one = points().get(0);
		Vector two = points().get(points.size()-1);
		float dx = two.x() - one.x();
		float dy = two.y() - one.y();
		Vector normal;
		if(this.isPointInFront(e.absolutePosition())) {
			normal = new DVector(-dy, dx);
		}else {
			normal = new DVector(dy, -dx);
		}
		
		return normal.normalise();
	}
	public Vector getNormal(boolean inFront) {
		Vector one = points().get(0);
		Vector two = points().get(points.size()-1);
		float dx = two.x() - one.x();
		float dy = two.y() - one.y();
		Vector normal;
		if(inFront) {
			normal = new DVector(-dy, dx);
		}else {
			normal = new DVector(dy, -dx);
		}
		
		return normal;
	}
	
	protected boolean isPointInFront(Vector position) {
		float[] abc = this.abc(points().get(0), points().get(points.size()-1));
		return abc[0]*(position.x()) + abc[1]*(position.y()) + abc[2] > 0;
	}
	
	protected boolean isPointInFrontOfLine(Vector position, float[] abc) {
		return abc[0]*(position.x()) + abc[1]*(position.y()) + abc[2] > 0;
	}
	
	protected Vector[] normals(Vector p1, Vector p2, Vector middle) {
		return new Vector[] {getNormal(true), getNormal(false)};
	}
	
	protected float[] abc(Vector p1, Vector p2) {
		float x1 = p1.x();
		float y1 = p1.y();
		float x2 = p2.x();
		float y2 = p2.y();
		float a = y1 - y2;
		float b = x2 - x1;
		float c = x1*y2 - y1*x2;
		return new float[] {a,b,c};
	}
	/**
	 * Once an entity has passed over the wall, this calculates the position the entity should be at and applies a force to it.
	 * It then updates the position of the entity
	 * 
	 * @param e
	 * 			The entity that has collided with this wall
	 * @param centre
	 * 			A vector representing the centre of the screen
	 */
	public void handleCollisions(Entity e, Vector centre){
		Vector normal = this.getNormal(e);
		Vector position = e.absolutePosition();
		Vector velocity = e.velocity().mult(-1);
		e.setPosition(this.getIntersection(position, velocity).add(normal.setMag(-0.1f)));
		e.setAcceleration(normal.mult(-1*this.bounciness()*(Vector.dot(e.velocity(), normal))/(normal.magSq())));
		e.updatePositionWithoutDrag();
	}
	

	@Override
	public Wall clone() {
		return this;
	}

}
