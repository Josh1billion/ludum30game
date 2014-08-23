package com.josh.graphicsengine2d;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.josh.graphicsengine2d.Globals;
import com.josh.graphicsengine2d.Light.LightType;
import com.josh.ludumgame.*;

public class Graphics
{
	public GL20 gl = null;
	public SpriteBatch batch; // for drawing text
	private Font currentFont = null;
	private OrthographicCamera camera = null;
	private ShaderProgram mainImageShader = null;

	public Light[] diffuseLights = new Light[10];
	public Light[] specularLights = new Light[10];
	Vector3 ambientLight = new Vector3(0.0f, 0.0f, 0.0f);
	
	private float fadeLevel = 1.0f;
	
	/*
	 * setZoom()
	 * Parameters:
	 * 	float zoomLevel - how far to zoom (1.0f = normal zoom, 0.5f = zoomed halfway out, 3.0f = zoomed in x3)
	*/
	public void setZoom(float zoomLevel)
	{
		if (zoomLevel > 0.0f && zoomLevel < 1.0f)
			zoomLevel = 1.0f / zoomLevel;
		else if (zoomLevel > 1.0f)
			zoomLevel = 1.0f / zoomLevel;
		camera.zoom = zoomLevel;
		camera.update();
	}
	
	public void setAmbientLight(float r0to255, float g0to255, float b0to255)
	{
		ambientLight.set(r0to255 / 255.0f, g0to255 / 255.0f, b0to255 / 255.0f);
	}
	
	public Light createDiffuseLight(float x, float y, float r0to255, float g0to255, float b0to255, float innerRadius, float outerRadius) throws Exception
	{
		int index = -1;
		for (int i = 0; i < 10; i++)
			if (diffuseLights[i] == null)
			{
				index = i;
				break;
			}

		if (index < 0)
			throw new Exception("Too many diffuse lights already exist.  Diffuse light could not be created in createDiffuseLight().");
		else
		{
			diffuseLights[index] = new Light(this, index, Light.LightType.LIGHT_DIFFUSE, x, y, r0to255, g0to255, b0to255, innerRadius, outerRadius);
			return diffuseLights[index];
		}
	}
	
	public void addLight(Light light, Light.LightType type) throws Exception
	{ // for lights that are manually created.  useful for custom lights that extend the Light class and must be created manually because of that.
		if (type == LightType.LIGHT_DIFFUSE)
		{
			int index = -1;
			for (int i = 0; i < 10; i++)
				if (diffuseLights[i] == null)
				{
					index = i;
					break;
				}
			if (index != -1)
				diffuseLights[index] = light;
			light.index = index;
		}
		else // specular light
		{
			int index = -1;
			for (int i = 0; i < 10; i++)
				if (specularLights[i] == null)
				{
					index = i;
					break;
				}
			if (index != -1)
				specularLights[index] = light;
			light.index = index;
		}
	}
	
	public void removeLight(Light light) throws Exception
	{
		for (int i = 0; i < 10; i++)
			if (diffuseLights[i] == light)
			{
				diffuseLights[i] = null;
				return;
			}
		
		for (int i = 0; i < 10; i++)
			if (specularLights[i] == light)
			{
				specularLights[i] = null;
				return;
			}
		
	}
	
	public void drawImage(Image image, float x, float y)
	{
		drawImage(image, x, y, 1.0f);
	}
	
	public void drawImage(Image image, float x, float y, float alpha)
	{
		drawImage(image, x, y, 1.0f, 1.0f, alpha);
	}
	
	public void drawImage(Image image, float x, float y, float alpha, float scaleX, float scaleY)
	{
		image.render(this, gl, mainImageShader, camera, x, y, scaleX, scaleY, alpha);
	}
	
	public void drawImage(Image image, float x, float y, float alpha, float scaleX, float scaleY, boolean flipHorizontal, boolean flipVertical)
	{
		image.render(this, gl, mainImageShader, camera, x, y, scaleX, scaleY, alpha, flipHorizontal, flipVertical);
	}
	
	public void drawImageSpecial(Image image, float x, float y, float alpha, float scaleX, float scaleY)
	{
		image.render(this, gl, mainImageShader, camera, x, y, scaleX, scaleY, alpha);
	}
	
	public void uploadLightsToShader(ShaderProgram shader, float screenX, float screenY)
	{ // called internally by Image.render().  no need to call it manually.
		shader.setUniformf("ambientLight", ambientLight);
		
		for (int i = 0; i < 10; i++)
			if (diffuseLights[i] != null)
				diffuseLights[i].activateOnShader(shader, screenX, screenY);
		
		for (int i = 0; i <10; i++)
			if (specularLights[i] != null)
				specularLights[i].activateOnShader(shader, screenX, screenY);
	}
	
	public void uploadFadeLevelToShader(ShaderProgram shader)
	{ // called internally by Image.render().  no need to call it manually.
		shader.setUniformf("fadeLevel", fadeLevel);		
	}
	
	
	public Graphics()
	{
		float w = Gdx.graphics.getWidth();
		float h = Gdx.graphics.getHeight();
		
		camera = new OrthographicCamera(w, h);
		camera.setToOrtho(true, w, h);
		camera.update();
		
		batch = new SpriteBatch();
		
		String textShaderVertex = "attribute vec4 a_position;\n" + 
				"attribute vec4 a_color;\n" + 
				"attribute vec2 a_texCoord0;\n" + 
				"uniform sampler2D u_texture;\n" + 
				"\n" + 
				"uniform mat4 u_projTrans;\n" + 
				"\n" + 
				"varying vec4 v_color;\n" + 
				"varying vec2 v_texCoords;\n" + 
				"\n" + 
				"void main() {\n" + 
				"    v_color = a_color;\n" + 
				"    v_texCoords = a_texCoord0;\n" +
				"	 float scaleAmount = 1.0f;" +
				"    gl_Position = u_projTrans * (a_position * mat4(scaleAmount, 0.0f, 0.0f, 0.0f, 0.0f, scaleAmount, 0.0f,  0.0f, 0.0f, 0.0f, scaleAmount, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f));\n" + 
				"}" ;
		
		String textShaderFragment = "#ifdef GL_ES\n" + 
				"    precision mediump float;\n" + 
				"#endif\n" + 
				"\n" + 
				"varying vec4 v_color;\n" + 
				"varying vec2 v_texCoords;\n" + 
				"uniform sampler2D u_texture;\n" + 
				"\n" + 
				"void main() {\n" + 
				"	 vec4 resultColor = v_color * texture2D(u_texture, v_texCoords);\n" +
				"	 \n" +
				"	 \n" + 
				"    gl_FragColor = resultColor;\n" + 
				"}";
		
		
		
		ShaderProgram textShader = new ShaderProgram(textShaderVertex, textShaderFragment);
		textShader.pedantic = false;
		if(!textShader.isCompiled()) {
	        Gdx.app.log("Problem compiling text shader:", textShader.getLog());
	    }
		
		//batch.setProjectionMatrix(new Matrix4(new float[] { scaleAmount, 0.0f, 0.0f, 0.0f, 0.0f, scaleAmount, 0.0f,0.0f, 0.0f, 0.0f, scaleAmount, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f}));
		batch.setShader(textShader);
		
		gl = Gdx.gl20;
		
		gl.glEnable(GL20.GL_TEXTURE_2D);
		gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
		gl.glEnable(GL20.GL_BLEND);
		
		String vertexShader = "attribute vec4 a_position;    \n" + 
				"attribute vec4 a_color;\n" +
				"attribute vec4 a_color2;\n" +
                "uniform vec3 ambientLight;\n" +

				"uniform vec3 lightPositions[10];\n" +
                "uniform vec3 lightColors[10];\n" +
                "uniform float lightInnerRadiuses[10];\n" +
                "uniform float lightOuterRadiuses[10];\n" +
                "uniform float scaleX;\n" +
                "uniform float scaleY;\n" +
                "uniform float texOffsetX;\n" +
                "uniform float texOffsetY;\n" +
                
                "uniform float texFlipX;\n" + // set to 1.0f for normal, or -1.0f to flip horizontally 
                "uniform float texFlipY;\n" +  // set to 1.0f for normal, or -1.0f to flip vertically
            
                "attribute vec2 a_texCoord0;\n" + 
                "uniform mat4 u_worldView;\n" +  
                "uniform mat4 u_transformation;\n" + 
                "varying vec4 v_color;" + 
                "varying vec2 v_texCoords;" + 
                "varying vec3 normal; \n" + 
                "varying vec3 vertex_to_light_vectors[10]; \n" + 
                "uniform float alpha;\n" +
                
                
                "void main()                  \n" + 
                "{                            \n" + 
                "   v_texCoords = a_texCoord0; \n" +
                "	v_texCoords[0] += texOffsetX;\n" +
                "	v_texCoords[1] += texOffsetY;\n" +
                "	v_texCoords[0] *= texFlipX;\n" +
                "	v_texCoords[1] *= texFlipY;\n" +
                
                "	mat4 scaleMatrix = mat4(vec4(scaleX, 0.0f, 0.0f, 0.0f), vec4(0.0f, scaleY, 0.0f, 0.0f), vec4(0.0f, 0.0f, 1.0f, 0.0f), vec4(0.0f, 0.0f, 0.0f, 1.0f));  \n" +

                "   gl_Position =  u_worldView  * (a_position * (scaleMatrix * u_transformation));  \n"      +     

				"    // Transforming The Normal To ModelView-Space\r\n" + 
				"    normal = gl_NormalMatrix * gl_Normal;\r\n" + 
				" \r\n" + 
				"    // Transforming The Vertex Position To ModelView-Space\r\n" + 
				"    vec4 vertex_in_modelview_space = gl_ModelViewMatrix * (a_position * u_transformation);\n" +
				"// Calculating The Vector From The Vertex Position To The Light Position\r\n" + 
				"	for (int i = 0; i < 10; i++) vertex_to_light_vectors[i] = lightPositions[i] - vertex_in_modelview_space.xyz;" +
				
                "}                            \n" ;
		String fragmentShader = "#ifdef GL_ES\n" +
                  "precision mediump float;\n" + 
                  "#endif\n" + 
                  "varying vec4 v_color;\n" + 
                  "varying vec2 v_texCoords;\n" + 
                  "uniform sampler2D u_texture;\n" + 
                  
                  "uniform vec3 ambientLight;\n" +
                  "uniform vec3 lightColors[10];\n" +
                  "uniform float lightInnerRadiuses[10];\n" +
                  "uniform float lightOuterRadiuses[10];\n" +
                  "uniform float fadeLevel;\n" +
                  "uniform float alpha;\n" +
                  
                  "varying vec3 normal; \n" + 
                  "varying vec3 vertex_to_light_vectors[10]; \n" + 
                  
                  "void main()                                  \n" + 
                  "{                                            \n" + 

                    "    vec4 combinedLightColor = vec4(0.0f, 0.0f, 0.0f, 1.0f); \n" +
                    "	 for (int i = 0; i < 10; i++)\n" +
                    "	   if (lightInnerRadiuses[i] > 0.0f || lightOuterRadiuses[i] > 0.0f) \n" +
                    "	   { \n" +
					"        vec4 DiffuseColor = vec4(lightColors[i], 1.0);\r\n" + 

					"        float lightInnerRadius = lightInnerRadiuses[i];\n" +
					"	     float lightOuterRadius = lightOuterRadiuses[i]; \n" +
					"	     float distanceToLight = length(vertex_to_light_vectors[i]);\n" +
				
					"        float DiffuseTerm = 1.0f;" +
					"	     if (distanceToLight < lightInnerRadius) DiffuseTerm = 1.0f; else { \n" +
					"          DiffuseTerm = 1.0f - (distanceToLight - lightInnerRadius)/lightOuterRadius;" +
					"	     } \n" +
					"	     DiffuseTerm = max(0.0f, min(1.0f, (DiffuseTerm)));	\n" +
					"	     combinedLightColor += vec4(DiffuseColor * DiffuseTerm); \n" +
					"	     combinedLightColor *= vec4(fadeLevel, fadeLevel, fadeLevel, 1.0f); \n" +
					"	   }\n" +
					"	combinedLightColor = vec4(min(1.0f, combinedLightColor.x), min(1.0f, combinedLightColor.y), min(1.0f, combinedLightColor.z), 0.0f); \n" +

                  "  gl_FragColor = (vec4(ambientLight, alpha) + combinedLightColor) * texture2D(u_texture, v_texCoords);\n" +
                  "}";
		
		 mainImageShader = new ShaderProgram(vertexShader, fragmentShader);
		 if (mainImageShader.isCompiled() == false)
		 {
	         Gdx.app.log("ShaderError in mainImageShader:", mainImageShader.getLog());
	         System.exit(0);
	     }
		 
	}
	
	public void setFont(Font font)
	{
		currentFont = font;
	}
		
	public void drawString(String textToDraw, int x, int y, int r0to255, int g0to255, int b0to255)
	{ // to-do: incorporate alpha?
		if (currentFont == null)
		{
			Gdx.app.debug("Error", "Error when calling drawString(): no font has been set yet (with Graphics.setFont())");
			return;
		}
		
		currentFont.render(batch, textToDraw, x, Globals.game.SCREEN_HEIGHT - y, r0to255, g0to255, b0to255);
		
		gl.glEnable(GL20.GL_TEXTURE_2D);
		gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
		gl.glEnable(GL20.GL_BLEND);
	}
	
	public void drawString(String textToDraw, int x, int y)
	{ // to-do: incorporate alpha?
		if (currentFont == null)
		{
			Gdx.app.debug("Error", "Error when calling drawText(): no font has been set yet (with Graphics.setFont())");
			return;
		}
		
		currentFont.render(batch, textToDraw, x, Gdx.graphics.getHeight() - y);
		
		gl.glEnable(GL20.GL_TEXTURE_2D);
		gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
		gl.glEnable(GL20.GL_BLEND);
	}
	
	public void setFade(float amount)
	{
		if (fadeLevel < 0.0f || fadeLevel > 1.0f)
			System.out.println("Error when calling setFadeLevel(): fade amount must be >= 0.0f and <= 1.0f");
		fadeLevel = amount;
	}
	
	// ticks lights and other time-sensitive elements.  should be called from Game.tick(), once per frame
	public void tick(float delta)
	{
		for (int i = 0; i < diffuseLights.length; i++)
			if (diffuseLights[i] != null)
				diffuseLights[i].tick(delta);
		
		for (int i = 0; i < specularLights.length; i++)
			if (specularLights[i] != null)
				specularLights[i].tick(delta);
	}
}
