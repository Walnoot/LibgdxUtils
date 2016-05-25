package walnoot.libgdxutils.world;

import com.badlogic.gdx.Files.FileType;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.PixmapPacker;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;

public class AssetHandler {
	private TextureAtlas atlas;

	private ObjectMap<String, FileHandle> fileHandles = new ObjectMap<>();
	private ObjectMap<String, Sprite> sprites = new ObjectMap<>();

	private Array<String> files;
	
	public AssetHandler(Array<String> files, AssetHandlerParameter params) {
		this.files = files;
		
		PixmapPacker packer = new PixmapPacker(params.pageWidth, params.pageHeight, params.pageFormat, params.padding,
				params.duplicateBorder);
		
		files.forEach(s -> {
			FileHandle f = Gdx.files.getFileHandle(params.prefix + s, params.fileType);
			
			fileHandles.put(s, f);
			
			if (s.matches(".*\\.(png|jpg|gif)")) {
				packer.pack(s, new Pixmap(f));
			}
		});
		
		usePacker(packer);
		
		atlas = packer.generateTextureAtlas(params.atlasMinFilter, params.atlasMagFilter, params.atlasUseMipmaps);
		
		for(AtlasRegion region : atlas.getRegions()) {
			sprites.put(region.name, atlas.createSprite(region.name));
		}
	}
	
	/**
	 * Allows to add stuff to the packer before the atlas is generated. Useful
	 * if you want to pack a font in the atlas for instance.
	 */
	protected void usePacker(PixmapPacker packer) {
	}
	
	public FileHandle getFileHandle(String file) {
		return fileHandles.get(file);
	}
	
	public Sprite getSprite(String file) {
		if (file != null && sprites.containsKey(file)) {
			return new Sprite(sprites.get(file));
		} else {
			return null;
		}
	}
	
	public Array<String> getFiles() {
		return files;
	}
	
	public static class AssetHandlerParameter {
		public FileType fileType = FileType.Internal;
		public String prefix = "";
		
		public int pageWidth = 1024, pageHeight = 1024;
		public Format pageFormat = Format.RGBA8888;
		public int padding = 2;
		public boolean duplicateBorder = false;
		
		public TextureFilter atlasMinFilter = TextureFilter.MipMapLinearLinear;
		public TextureFilter atlasMagFilter = TextureFilter.Linear;
		public boolean atlasUseMipmaps = true;
	}
}
