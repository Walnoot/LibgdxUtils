package walnoot.libgdxutils.world.editor;

import java.util.function.Consumer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.List;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.SplitPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.Pools;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

import walnoot.libgdxutils.State;
import walnoot.libgdxutils.world.GameWorld;
import walnoot.libgdxutils.world.components.SpritesComponent;

public class EditorState extends State {
	private Stage stage;
	private Object comp;
	private WorldView worldView;
	private GameWorld world;
	private Skin skin;

	public EditorState(GameWorld world) {
		this.world = world;
		
		skin = new Skin(Gdx.files.classpath("walnoot/libgdxutils/world/editor/uiskin.json"));

		skin.getFont("default-font").getData().markupEnabled = true;
		
		//override scroll method to disregard the scroll focus
		stage = new Stage(new ScreenViewport()) {
			private Vector2 tempCoords = new Vector2();
			
			@Override
			public boolean scrolled(int amount) {
				screenToStageCoordinates(tempCoords.set(Gdx.input.getX(), Gdx.input.getY()));
				
				Actor target = hit(tempCoords.x, tempCoords.y, true);
				if (target == null) target = stage.getRoot();
				
				InputEvent event = Pools.obtain(InputEvent.class);
				event.setStage(this);
				event.setType(InputEvent.Type.scrolled);
				event.setScrollAmount(amount);
				event.setStageX(tempCoords.x);
				event.setStageY(tempCoords.y);
				target.fire(event);
				boolean handled = event.isHandled();
				Pools.free(event);
				return handled;
			}
		};
		Gdx.input.setInputProcessor(stage);
		
		Table fullTable = new Table(skin);
		fullTable.setFillParent(true);
		
		ScrollPane pane = new ScrollPane(null, skin, "small");
		
		pane.addCaptureListener(new InputListener() {
			@Override
			public boolean scrolled(InputEvent event, float x, float y, int amount) {
				System.out.println(amount);
				
				return super.scrolled(event, x, y, amount);
			}
		});
		
		stage.setScrollFocus(pane);
		pane.setFadeScrollBars(false);
		pane.setFlickScroll(false);
		pane.setOverscroll(false, false);
		pane.setSmoothScrolling(false);
		
		EditPane editPane = new EditPane(this);
//		comp = new TestClass();
		comp = world.stream().findAny().get().get(SpritesComponent.class);
		
		Table paneContent = new Table(skin);

		editPane.addObject(comp, paneContent);
		
		pane.setWidget(paneContent);
		
		worldView = new WorldView(world);
		SplitPane splitPane = new SplitPane(pane, worldView, false, skin);
		splitPane.setSplitAmount(0.25f);
		fullTable.add(splitPane).expand().fill();
		
		stage.addActor(fullTable);
	}
	
	@Override
	public void update() {
//		world.update(getDelta());
	}
	
	@Override
	public void render() {
	    Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
	    stage.act(Gdx.graphics.getDeltaTime());
	    
	    stage.draw();
	    
	    if(Gdx.input.isKeyJustPressed(Keys.ENTER)) {
	    	System.out.println(new Json().toJson(comp));
	    }
	    
	    if(Gdx.input.isKeyJustPressed(Keys.TAB)) addSelectDialog(world.getAssetHandler().getFiles(), (s) -> System.out.println(s));
	    
	    worldView.render();
	}
	
	@Override
	public void resize(boolean creation, int width, int height) {
		stage.getViewport().update(width, height, true);
	}
	
	public <T> void addSelectDialog(Array<T> items, Consumer<T> consumer) {
		Table blockTable = new Table(skin);
		blockTable.setFillParent(true);
		blockTable.setBackground("backgroundDim");
		stage.addActor(blockTable);
		
		List<T> list = new List<T>(skin);
		list.setItems(items);
		
		ScrollPane scrollPane = new ScrollPane(list, skin);
		
		Dialog dialog = new Dialog("Idk", skin) {
			@Override
			protected void result(Object object) {
				if((Boolean) object) {
					consumer.accept(list.getSelected());
				}
				
				super.result(object);
			}
			
			@Override
			public void hide() {
				getParent().remove();
			}
		};
		
		dialog.getContentTable().add(scrollPane).expandX().fillX();
		
		dialog.button("Cancel", false);
		dialog.button("Confirm", true);
		dialog.setSize(300f, 200f);
		dialog.setX((stage.getWidth() - dialog.getWidth()) / 2);
		dialog.setY((stage.getHeight() - dialog.getHeight()) / 2);
		
		blockTable.add(dialog).size(300f, 200f);
		
		stage.setKeyboardFocus(null);
	}
	
	public GameWorld getWorld() {
		return world;
	}
	
	public static class TestClass {
		public TestClass() {
		}
		
		int a;
		float b;
		char c = 'c';
		TestClass dddd;
		String e;
		Vector2 f = new Vector2(1, 2);
//		BodyDef g = new BodyDef();
		float[] h = {1,2,3,4,5};
//		String[] h = new String[] {null, null, null, null};
//		Color[] h = new Color[] {new Color(0xFF0000), new Color(0x00FF00), null};
		Array<String> i = new Array<String>();
		
		{
			i.add("hoi");
			i.add("hoi2");
			i.add("hoi3");
			i.add(null);
		}
	}
}
