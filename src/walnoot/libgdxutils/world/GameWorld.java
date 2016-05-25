package walnoot.libgdxutils.world;

import java.util.ArrayList;
import java.util.function.Consumer;
import java.util.stream.Stream;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.Array;

import walnoot.libgdxutils.world.components.BodyDefComponent;
import walnoot.libgdxutils.world.components.FixturesComponent;

public class GameWorld implements ContactListener {
	private final BodyDef defaultDef = BodyDefComponent.getDefaultDef();
	
	private World world = new World(new Vector2(0f, 0f), true);
	
	private Array<Body> tmpBodies = new Array<>();
	private Array<Entity> removedEntities = new Array<>(); //entities that will be removed after updating
	
	private PrototypeLoader loader;
	private AssetHandler assetHandler;
	
	private RadiusSearcher searcher = new RadiusSearcher();
	
	public GameWorld(AssetHandler handler) {
		this.assetHandler = handler;
		
		this.loader = new PrototypeLoader(handler);
		
		world.setContactListener(this);
	}

	public void update(float delta) {
		world.step(delta, 8, 3);
		
		forAllEntities((e) -> e.update());
		
		for(Entity e : removedEntities) {
			e.onRemove();
			
			if(e.getBody() != null) world.destroyBody(e.getBody());
			e.setWorld(null, null);
		}
		
		removedEntities.size = 0;
	}
	
	public void forAllEntities(Consumer<Entity> c) {
		world.getBodies(tmpBodies);
		for (int i = 0; i < tmpBodies.size; i++) {
			c.accept((Entity) tmpBodies.get(i).getUserData());
		}
	}
	
	public Stream<Entity> stream() {
		ArrayList<Entity> entities = new ArrayList<>();
		forAllEntities((e) -> entities.add(e));
		
		return entities.stream();
	}
	
	public void queryRadius(Vector2 pos, float radius, Consumer<Entity> c) {
		searcher.startSearch(QueryType.IN_RADIUS, pos, radius, c);
		world.QueryAABB(searcher, pos.x - radius, pos.y - radius, pos.x + radius, pos.y + radius);
		searcher.endSearch();
	}
	
	public void queryPoint(Vector2 pos, Consumer<Entity> c) {
		searcher.startSearch(QueryType.OVERLAP_AABB, pos, 0f, c);
		world.QueryAABB(searcher, pos.x, pos.y, pos.x, pos.y);
		searcher.endSearch();
	}
	
	public Entity addEntity(Entity e) {
		BodyDefComponent defComponent = e.get(BodyDefComponent.class);
		Body body = world.createBody(defComponent == null ? defaultDef : defComponent.def);
		body.setUserData(e);
		
		if (e.has(FixturesComponent.class)) {
			for (FixtureDef def : e.get(FixturesComponent.class).fixtures) {
				body.createFixture(def);
			}
		}
		
		e.setWorld(this, body);
		e.forAllComponents((c) -> c.init());
		
		return e;
	}
	
	public Entity addEntity(String proto) {
		Entity entity = addEntity(loader.createProto(proto));
		
		return entity;
	}
	
	public void removeEntity(Entity e) {
		removedEntities.add(e);
	}
	
	@Override
	public void beginContact(Contact contact) {
		Entity a = (Entity) contact.getFixtureA().getBody().getUserData();
		Entity b = (Entity) contact.getFixtureB().getBody().getUserData();

		a.beginContact(contact, b, contact.getFixtureA(), contact.getFixtureB());
		b.beginContact(contact, a, contact.getFixtureB(), contact.getFixtureA());
	}
	
	@Override
	public void endContact(Contact contact) {
		Entity a = (Entity) contact.getFixtureA().getBody().getUserData();
		Entity b = (Entity) contact.getFixtureB().getBody().getUserData();

		a.endContact(contact, b, contact.getFixtureA(), contact.getFixtureB());
		b.endContact(contact, a, contact.getFixtureB(), contact.getFixtureA());
	}
	
	@Override
	public void preSolve(Contact contact, Manifold oldManifold) {
	}
	
	@Override
	public void postSolve(Contact contact, ContactImpulse impulse) {
	}
	
	public World getBox2d() {
		return world;
	}
	
	public AssetHandler getAssetHandler() {
		return assetHandler;
	}
	
	private class RadiusSearcher implements QueryCallback {
		private QueryType type;
		private Vector2 pos = null;
		private float radius;
		private Consumer<Entity> c;
		
		private void startSearch(QueryType type, Vector2 pos, float radius, Consumer<Entity> c) {
			if(this.pos != null) {
				throw new IllegalStateException("Already searching radius!");
			} else {
				this.type = type;
				this.pos = pos;
				this.radius = radius;
				this.c = c;
			}
		}
		
		private void endSearch() {
			pos = null;
			radius = 0f;
			c = null;
		}
		
		@Override
		public boolean reportFixture(Fixture fixture) {
			if(type == QueryType.OVERLAP_AABB || fixture.getBody().getPosition().dst2(pos) < radius * radius) {
				c.accept((Entity) fixture.getBody().getUserData());
			}
			
			return true;
		}
	}
	
	public static enum QueryType {
		IN_RADIUS, OVERLAP_AABB;
	}
}
