package game;
import java.util.ArrayList;

import logic.DVector;
import logic.Vector;
import processing.core.*;

public abstract class AbstractWall implements Wall {
	ArrayList<Vector> points = new ArrayList<Vector>();
	
	public AbstractWall(ArrayList<Vector> points, Vector center){
		this.points = points;
		this.move(center);
	}
	
	public AbstractWall(ArrayList<Vector> points){
		this.points = points;
	}
	
	AbstractWall(float x1, float y1, float x2, float y2, Vector center){
		this.points.add(new DVector(x1, y1));
		this.points.add(new DVector(x2, y2));
		this.move(center);
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
		app.line(this.points.get(0).x() + offset.x(), this.points.get(0).y() + offset.y(), this.points.get(this.points.size()-1).x() + offset.x(), this.points.get(this.points.size()-1).y() + offset.y());
	}
	
	@Override
	public boolean getPlayerSide(Vector absolutePosition) {  	
		return isPointInFront(absolutePosition);//if wall.a*player.x + wall.b*player.y + wall.c > 0, player counts as being in front of wall, otherwise, player is behind wall
	}
	
	@Override
	public boolean inBounds(Vector absolutePosition) {
		ArrayList<Vector> points = points();
		Vector[] relativeNormals1 = normals(points.get(0), points.get(points.size()-1), points.get(0));
		Vector normal1 = relativeNormals1[0].add(points.get(0));
		Vector normal2 = relativeNormals1[1].add(points.get(0));
		Vector normala = relativeNormals1[0].add(points.get(points.size()-1));
		Vector normalb = relativeNormals1[1].add(points.get(points.size()-1));
		//if the point is in front of the normal to the wall at the first point and behind the normal to the wall at the last point or vice versa, return true 
		return isPointInFrontOfLine(absolutePosition, abc(normal1, normal2)) && !isPointInFrontOfLine(absolutePosition, abc(normala, normalb)) || !isPointInFrontOfLine(absolutePosition, abc(normal1, normal2)) && isPointInFrontOfLine(absolutePosition, abc(normala, normalb));
	}
	
	public Vector getIntersection(Vector position, Vector velocity) {
		float[] abc = abc(position, velocity);
		float[] ABC = abc(points.get(0), points.get(points.size()-1));
		float x = (abc[1]*ABC[2] - ABC[1]*abc[2])/(ABC[1]*abc[0] - abc[1]*ABC[0]); // x = (bC - Bc)/(Ba - bA)
		float y = (abc[0]*ABC[2] - ABC[0]*abc[2])/(ABC[0]*abc[1] - abc[0]*ABC[1]); // y = (aC - Ac)/(Ab - aB)
		return new DVector(x,y);
	}
	
	public Vector getNormal(Entity e) {
		Vector one = points.get(0);
		Vector two = points.get(points.size()-1);
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
		Vector one = points.get(0);
		Vector two = points.get(points.size()-1);
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
		float[] abc = this.abc(points.get(0), points.get(points.size()-1));
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
		e.setPosition(this.getIntersection(e.position(), e.velocity().mult(-1)));
		Vector normal = this.getNormal(e);
		e.setAcceleration(normal.mult(-1*this.bounciness()*(Vector.dot(e.velocity(), normal))/(normal.magSq())));
		e.updatePositionWithoutDrag();
	}

	@Override
	public Wall clone() {
		return this;
	}

}