package walnoot.libgdxutils.world.editor;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.SnapshotArray;

public class EditPane {
	private static final HashMap<Class<?>, Class<?>> PRIMITIVES = new HashMap<>();
	
	{
		PRIMITIVES.put(Byte.class, byte.class);
		PRIMITIVES.put(Short.class, short.class);
		PRIMITIVES.put(Integer.class, int.class);
		PRIMITIVES.put(Long.class, long.class);
		PRIMITIVES.put(Boolean.class, boolean.class);
		PRIMITIVES.put(Character.class, char.class);
		PRIMITIVES.put(Float.class, float.class);
		PRIMITIVES.put(Double.class, double.class);
	}
	
	private ObjectMap<Class<?>, ClassActor<?, ?>> classActors = new ObjectMap<>();
	private ClassActor<Object, SelectBox<?>> enumClassActor;
	private ClassActor<Object, Actor> arrayClassActor;
	
	private ObjectMap<Class<?>, Supplier<?>> classProducers = new ObjectMap<>();
	
	private ObjectMap<Object, Class<?>[]> genericParameters = new ObjectMap<>();
	
	public EditPane(EditorState editorState) {
		//class actors for primitive types
		addClassActor(boolean.class,
				(skin, bool) -> {
					CheckBox box = new CheckBox(null, skin);
					box.setChecked(bool);
					
					return box;
				},
				(CheckBox box) -> box.isChecked(), false);
		
		addTextFieldActorSupplier(float.class, f -> Float.toString(f), Float::parseFloat);
		addTextFieldActorSupplier(double.class, f -> Double.toString(f), Double::parseDouble);
		addTextFieldActorSupplier(int.class, f -> Integer.toString(f), Integer::parseInt);
		addTextFieldActorSupplier(long.class, f -> Long.toString(f), Long::parseLong);
		addTextFieldActorSupplier(short.class, f -> Short.toString(f), Short::parseShort);
		addTextFieldActorSupplier(byte.class, f -> Byte.toString(f), Byte::parseByte);
		addTextFieldActorSupplier(char.class, f -> Character.toString(f), s -> {
			if(s.length() != 1) throw new IllegalStateException();
			
			return s.charAt(0);
		});
		
		addTextFieldActorSupplier(String.class, f -> f, f -> f);
		
		//class actor for generic objects, lists fields
		addClassActor(Object.class, (Skin skin, Object object) -> {
			Table target = new Table(skin);
			
			for (Field field : object.getClass().getDeclaredFields()) {
				if (!(Modifier.isTransient(field.getModifiers()) || Modifier.isStatic(field.getModifiers())
						|| field.isSynthetic())) {
					field.setAccessible(true);
					
					Label label = new Label(field.getName(), skin);
					label.setWrap(true);
					target.add(label).left().top().maxWidth(50f);
					
					try {
						Object value = field.get(object);
						
						Type type = field.getGenericType();
						if(type instanceof ParameterizedType) {
							ParameterizedType parameterizedType = (ParameterizedType) type;
							Type[] typeArguments = parameterizedType.getActualTypeArguments();
							
							Class<?>[] genericTypes = new Class<?>[typeArguments.length];
							boolean allClasses = true;
							for (int i = 0; i < genericTypes.length; i++) {
								if(typeArguments[i] instanceof Class<?>) {
									genericTypes[i] = (Class<?>) typeArguments[i];
								} else {
									allClasses = false;
								}
							}
							
							if(allClasses) {
								genericParameters.put(value, genericTypes);
							}
						}
						ClassActor<?, ?> classActor = findClassActor(value == null, field.getType());
						
						Actor actor = getActor(value, field.getType(), skin);
						
						actor.addListener(new ChangeListener() {
							@Override
							public void changed(ChangeEvent event, Actor changedActor) {
								changedActor.setColor(Color.WHITE);
								
								try {
									if (classActor.objectUpdater != null) {
										field.set(object, classActor.objectUpdater.apply(actor));
									} else {
										System.out.println("erreur");
									}
								} catch (NumberFormatException e) {
									changedActor.setColor(Color.GRAY);
								}catch (Exception e) {
									e.printStackTrace();
									changedActor.setColor(Color.GRAY);
								}
							}
						});
						
						if (classActor.newline) {
							target.row();
							target.add(actor).colspan(2).expandX().fill().padLeft(8f).row();
						} else {
							target.add(actor).expandX().right().row();
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
			
			return target;
		}, (Table t) -> ((ObjectEntry) t.getUserObject()).entry, true);
		
		//for enums, enum values as selectbox
		enumClassActor = new ClassActor<>(Object.class,
				(skin, object) -> {
					SelectBox<Object> box = new SelectBox<>(skin);
					
					box.setItems(object.getClass().getEnumConstants());
					box.setSelected(object);
					
					return box;
				},
				(SelectBox<?> box) -> box.getSelected(),
				false);
		
		//actors for native arrays, and libgdx Array type
		arrayClassActor = new ClassActor<>(null, (Skin skin, Object array) -> {
			return createArrayActor(skin, array);
		}, (Actor actor) -> {
			return updateArray(actor);
		}, true);
		
		ClassActor<?, ?> arrayObjectClassActor = new ClassActor<>(com.badlogic.gdx.utils.Array.class, (Skin skin, com.badlogic.gdx.utils.Array array) -> {
				ArrayList<Object> list = new ArrayList<>();
				
				for(Object obj : array) {
					list.add(obj);
				}
				
				return createCollectionActor(skin, genericParameters.get(array)[0], list);
			}, (ArrayItemTable actor) -> {
				List<Object> items = getArrayItems(actor);
				Object entry = ((ObjectEntry) actor.getUserObject()).entry;
				com.badlogic.gdx.utils.Array<Object> array = (com.badlogic.gdx.utils.Array<Object>) entry;
				
				array.clear();
				
				for(Object item : items) {
					array.add(item);
				}
				
				return array;
			}, true);
		addClassActor(arrayObjectClassActor);
		
		//test for dialog
		addClassActor(String.class, (Skin skin, String string) -> {
			Table table = new Table(skin);
			TextButton button = new TextButton(string, skin);
			button.setName("button");
			table.add(button);
			
			button.addListener(new ChangeListener() {
				@Override
				public void changed(ChangeEvent event, Actor actor) {
					event.cancel();
					
					editorState.addSelectDialog(editorState.getWorld().getAssetHandler().getFiles(), (s) -> {
							System.out.println("go");
							button.setUserObject(s);
							button.setText(s);
							table.fire(new ChangeEvent());
						});
				}
			});
			
			return table;
		},  (Table a) -> {
			System.out.println("changed");
			
			return (String) a.findActor("button").getUserObject();
		}, false);
		
		//default values of primitive types
		addClassProducer(byte.class, () -> (byte) 0);
		addClassProducer(short.class, () -> (short) 0);
		addClassProducer(int.class, () -> 0);
		addClassProducer(long.class, () -> 0l);
		addClassProducer(float.class, () -> 0f);
		addClassProducer(double.class, () -> 0.0);
		addClassProducer(char.class, () -> ' ');
		addClassProducer(boolean.class, () -> false);
	}

	private Actor createArrayActor(Skin skin, Object array) {
		Class<?> clazz = array.getClass().getComponentType();
		ArrayList<Object> list = new ArrayList<>();
		
		for (int i = 0; i < Array.getLength(array); i++) {
			list.add(Array.get(array, i));
		}
		
		return createCollectionActor(skin, clazz, list);
	}

	private Actor createCollectionActor(Skin skin, Class<?> clazz, List<?> items) {
		ArrayItemTable table = new ArrayItemTable(skin, clazz);
		ArrayItemTable itemsTable = new ArrayItemTable(skin, clazz);

		table.add(itemsTable).expand().fillX().row();
		
		int length = items.size();
		
		for(int i = 0; i < length; i++){
			Table itemTable = new Table(skin);
			
			TextButton upButton = new TextButton(" ^ ", skin);
			upButton.addListener(new ChangeListener() {
				@Override
				public void changed(ChangeEvent event, Actor actor) {
					moveItem(itemsTable, itemTable, -1);
				}
			});
			itemTable.add(upButton).left();
			
			TextButton downButton = new TextButton(" v ", skin);
			downButton.addListener(new ChangeListener() {
				@Override
				public void changed(ChangeEvent event, Actor actor) {
					moveItem(itemsTable, itemTable, 1);
				}
			});
			itemTable.add(downButton).left();
			
			TextButton removeButton = new TextButton(" X ", skin);
			removeButton.addListener(new ChangeListener() {
				@Override
				public void changed(ChangeEvent event, Actor actor) {
					itemsTable.removeActor(itemTable);
				}
			});
			itemTable.add(removeButton).left().expand();
			
			Object value = items.get(i);
			
			Actor actor = getActor(value, clazz, skin);
			actor.setName("item");
			
			if(findClassActor(value == null, clazz).newline) {
				itemTable.row();
				itemTable.add(actor).expand().fill().colspan(3).padLeft(8f);
			} else {
				itemTable.add(actor).expand().right();
			}
			
			
			itemsTable.add(itemTable).expand().fillX().row();
		}
		
		table.add(new TextButton("Add..", skin)).right().row();
		
		return table;
	}
	
	private void moveItem(Table table, Table itemTable, int shift) {
		ArrayList<Actor> actors = new ArrayList<Actor>();
		
		SnapshotArray<Actor> children = table.getChildren();
		int index = 0;
		for (int i = 0; i < children.size; i++) {
			if (children.get(i) == itemTable) {
				index = i;
			} else {
				actors.add(children.get(i));
			}
		}
		
		if(index + shift >= children.size || index + shift < 0) return;
		
		table.clearChildren();
		
		actors.add(index + shift, itemTable);
		
		for(Actor item : actors) {
			table.add(item).expand().fill().row();
		}
	}
	
	//creates an array based on the actor
	private Object updateArray(Actor actor) {
		ArrayItemTable table = (ArrayItemTable) actor;
		Class<? extends Object> clazz = table.clazz;
		List<Object> items = getArrayItems((ArrayItemTable) table);
		
		Object instance = Array.newInstance(clazz, items.size());
		
		for(int i = 0; i < items.size(); i++) {
			Array.set(instance, i, items.get(i));
		}
		
		return instance;
	}
	
	//gets all items represented by the table, assuming the layout is like createCollectionActor()
	private List<Object> getArrayItems(ArrayItemTable table) {
		ArrayList<Object> list = new ArrayList<>();
		
		Table items = (Table) table.getChildren().first();
		int numChildren = items.getChildren().size;
		for(int i = 0; i < numChildren; i++) {
			Actor itemActor = ((Table) items.getChildren().get(i)).findActor("item");
			Object item = ((ObjectEntry) itemActor.getUserObject()).classActor.objectUpdater.apply(itemActor);
			list.add(item);
		}
		
		TextButton newButton = (TextButton) table.getChildren().get(1);
		if(newButton.isChecked()) {
			newButton.setChecked(false);
			
			list.add(newInstance(table.clazz));
			
			table.fire(new RebuildEvent());
		}
		
		return list;
	}
	
	public void addObject(Object object, Table target) {
//		target.debugAll();
		
		target.add(getActor(object, object.getClass(), target.getSkin())).expand().fillX().top().pad(6f);
		target.addListener(new EventListener() {
			@Override
			public boolean handle(Event event) {
				if (event instanceof RebuildEvent) {
					Gdx.app.postRunnable(() -> {
						target.clearChildren();
						target.add(getActor(object, object.getClass(), target.getSkin())).expand().fillX().top().pad(6f);
					});
				}
				
				return false;
			}
		});
	}
	
	private Actor getActor(Object object, Class<?> clazz, Skin skin) {
		ClassActor<?, ?> classActor = findClassActor(object == null, clazz);
		
		Actor actor = classActor.actorSupplier.apply(skin, object);
		actor.setUserObject(new ObjectEntry(object, classActor));
		
		return actor;
	}
	
	private ClassActor<?, ?> findClassActor(boolean isNull, final Class<?> clazz) {
		if(isNull) {
			ClassActor<?, ?> nullClassActor = new ClassActor<>(null, (Skin skin, Object obj) -> {
					TextButton button = new TextButton("Create..", skin);
					button.addListener(new ChangeListener() {
						@Override
						public void changed(ChangeEvent event, Actor changedActor) {
							changedActor.fire(new RebuildEvent());
						}
					});
					
					return button;
				}, (TextButton button) -> {
					if(button.isChecked()) {
						try {
							return clazz.newInstance();
						} catch (Exception e) {
							e.printStackTrace();
							return null;
						}
					} else {
						return null;
					}
				}, false);
			
			return nullClassActor;
		}
		
		ClassActor<?, ?> classActor = getClassActor(clazz);
		
		Class<?> tempClazz = clazz;
		while (classActor == null) {
			tempClazz = tempClazz.getSuperclass();
			classActor = getClassActor(tempClazz);
		}
		
		return classActor;
	}
	
	private ClassActor<?, ?> getClassActor(Class<?> clazz) {
		if(clazz.isEnum()) {
			return enumClassActor;
		} else if(classActors.containsKey(clazz)) {
			return classActors.get(clazz);
		} else if(clazz.isArray()) {
			return arrayClassActor;
		} else if(PRIMITIVES.containsKey(clazz)) {
			return classActors.get(PRIMITIVES.get(clazz));
		} else {
			return null;
		}
	}
	
	public <T, V extends Actor> void addClassActor(Class<T> clazz, BiFunction<Skin, T, V> supplier,
			Function<V, T> updateObject, boolean newline) {
		ClassActor<T, V> actor = new ClassActor(clazz, supplier, updateObject, newline);
		
		addClassActor(actor);
	}
	
	public void addClassActor(ClassActor<?, ?> classActor) {
		classActors.put(classActor.clazz, classActor);
	}
	
	public <T> void addTextFieldActorSupplier(Class<T> clazz, Function<T, String> toString,
			Function<String, T> fromString) {
		ClassActor<T, TextField> actor = new ClassActor<>(clazz, (skin, value) -> new TextField(toString.apply(value), skin),
				field -> fromString.apply(((TextField) field).getText()), false);
		
		addClassActor(actor);
	}
	
	public <T> void addClassProducer(Class<T> clazz, Supplier<T> supplier) {
		classProducers.put(clazz, supplier);
	}
	
	public Object newInstance(Class<?> clazz) {
		if(classProducers.containsKey(clazz)) {
			return classProducers.get(clazz).get();
		}
		
		if(PRIMITIVES.containsKey(clazz)) {
			return newInstance(PRIMITIVES.get(clazz));
		}
		
		try {
			Object instance = clazz.newInstance();
			
			return instance;
		} catch (InstantiationException | IllegalAccessException e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	public static class ClassActor<T, V extends Actor> {
		private Class<T> clazz;
		
		private BiFunction<Skin, Object, Actor> actorSupplier;
		private Function<Actor, Object> objectUpdater;
		
		private boolean newline;
		
		@SuppressWarnings("unchecked")
		public ClassActor(Class<T> clazz, BiFunction<Skin, T, Actor> actorSupplier, Function<V, T> objectUpdater,
				boolean newline) {
			this.clazz = clazz;
			this.actorSupplier = (BiFunction<Skin, Object, Actor>) actorSupplier;
			this.objectUpdater = (Function<Actor, Object>) objectUpdater;
			this.newline = newline;
		}
	}
	
	public static class RebuildEvent extends Event {
	}
	
	private class ArrayItemTable extends Table {
		private Class<?> clazz;

		public ArrayItemTable(Skin skin, Class<?> clazz) {
			super(skin);
			this.clazz = clazz;
		}
	}
	
	private class ObjectEntry {
		private Object entry;
		private ClassActor<?, ?> classActor;
		
		private Class<?>[] genericParameters;
		
		public ObjectEntry(Object entry, ClassActor<?, ?> classActor) {
			this.entry = entry;
			this.classActor = classActor;
		}
	}
}
