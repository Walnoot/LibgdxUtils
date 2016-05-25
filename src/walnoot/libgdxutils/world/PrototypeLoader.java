package walnoot.libgdxutils.world;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.Shape;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.Json.ReadOnlySerializer;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.ObjectMap;

public class PrototypeLoader {
	private ObjectMap<String, JsonValue> prototypes = new ObjectMap<>();
	private Json json = new Json();
	private JsonValue jsonValue;
	
	public PrototypeLoader(AssetHandler handler) {
		FileHandle fh = Gdx.files.internal("proto.json");
		
		if(fh.exists()) {
			jsonValue = new JsonReader().parse(fh);
			
			JsonValue proto = jsonValue.child;
			while (proto != null) {
				prototypes.put(proto.name, proto);
				
				proto = proto.next;
			}
			
			json.setSerializer(Shape.class, new ReadOnlySerializer<Shape>() {
				@Override
				@SuppressWarnings("rawtypes")
				public Shape read(Json json, JsonValue jsonData, Class type) {
					if (jsonData.getString(0).equals("box")) {
						PolygonShape box = new PolygonShape();
						Vector2 pos = new Vector2();
						
						if(jsonData.get(4) != null) {
							pos.set(jsonData.getFloat(3), jsonData.getFloat(4));
						}
						
						box.setAsBox(jsonData.getFloat(1), jsonData.getFloat(2), pos, 0f);
						
						return box;
					} else if (jsonData.getString(0).equals("circle")) {
						CircleShape circle = new CircleShape();
						circle.setRadius(jsonData.getFloat(1));
						
						return circle;
					}
					
					return null;
				}
			});
		}
	}
	
	public Entity createProto(String name) {
		Entity entity = new Entity();
		
		Component[] components = json.readValue(name, Component[].class, jsonValue);
		for(Component c : components) {
			entity.addComponent(c);
		}
		
		return entity;
	}
}
