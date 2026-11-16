#include <jni.h>
#include <GLES3/gl3.h>
#include <android/log.h>
#include <cmath>
#include <vector>

#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, "StormNative", __VA_ARGS__)
static GLuint program = 0, vbo = 0;
static GLint posLoc = -1, colorLoc = -1;
static float moveX = 0.f, moveY = 0.f;
static bool isAttack = false, isGuard = false, isJump = false, isChakra = false;
static int screenW = 1, screenH = 1;

static const char* VS = R"(
#version 300 es
layout(location=0) in vec2 aPos;
uniform vec4 uColor;
out vec4 vColor;
void main(){ gl_Position=vec4(aPos,0.0,1.0); vColor=uColor; }
)";
static const char* FS = R"(
#version 300 es
precision mediump float;
in vec4 vColor;
out vec4 fragColor;
void main(){ fragColor=vColor; }
)";

static GLuint compile(GLenum type, const char* src) {
    GLuint s=glCreateShader(type); glShaderSource(s,1,&src,nullptr); glCompileShader(s);
    GLint ok=0; glGetShaderiv(s,GL_COMPILE_STATUS,&ok);
    if(!ok){ char log[1024]; glGetShaderInfoLog(s,sizeof(log),nullptr,log); LOGE("shader: %s",log); glDeleteShader(s); return 0; }
    return s;
}
static void rect(float x1,float y1,float x2,float y2,float r,float g,float b) {
    GLfloat v[]={x1,y1,x2,y1,x2,y2,x1,y1,x2,y2,x1,y2};
    glUniform4f(colorLoc,r,g,b,1.f); glVertexAttribPointer(0,2,GL_FLOAT,GL_FALSE,0,v); glEnableVertexAttribArray(0); glDrawArrays(GL_TRIANGLES,0,6);
}
static void circle(float cx,float cy,float rx,float ry,float r,float g,float b,int seg=48) {
    std::vector<GLfloat> v; v.reserve((seg+2)*2); v.push_back(cx); v.push_back(cy);
    for(int i=0;i<=seg;i++){ float a=6.2831853f*i/seg; v.push_back(cx+std::cos(a)*rx); v.push_back(cy+std::sin(a)*ry); }
    glUniform4f(colorLoc,r,g,b,1.f); glVertexAttribPointer(0,2,GL_FLOAT,GL_FALSE,0,v.data()); glEnableVertexAttribArray(0); glDrawArrays(GL_TRIANGLE_FAN,0,seg+2);
}
extern "C" JNIEXPORT void JNICALL Java_com_stormrev_nativeprototype_NativeBridge_init(JNIEnv*,jobject){
    GLuint vs=compile(GL_VERTEX_SHADER,VS), fs=compile(GL_FRAGMENT_SHADER,FS); if(!vs||!fs)return;
    program=glCreateProgram(); glAttachShader(program,vs); glAttachShader(program,fs); glLinkProgram(program);
    GLint ok=0; glGetProgramiv(program,GL_LINK_STATUS,&ok); glDeleteShader(vs); glDeleteShader(fs);
    if(!ok){ LOGE("program link failed"); glDeleteProgram(program); program=0; return; }
    colorLoc=glGetUniformLocation(program,"uColor"); glDisable(GL_DEPTH_TEST); glClearColor(.035f,.055f,.085f,1.f);
}
extern "C" JNIEXPORT void JNICALL Java_com_stormrev_nativeprototype_NativeBridge_resize(JNIEnv*,jobject,jint w,jint h){ screenW=w; screenH=h; glViewport(0,0,w,h); }
extern "C" JNIEXPORT void JNICALL Java_com_stormrev_nativeprototype_NativeBridge_setInput(JNIEnv*,jobject,jfloat x,jfloat y,jboolean a,jboolean g,jboolean j,jboolean c){ moveX=x; moveY=y; isAttack=a; isGuard=g; isJump=j; isChakra=c; }
extern "C" JNIEXPORT void JNICALL Java_com_stormrev_nativeprototype_NativeBridge_draw(JNIEnv*,jobject){
    if(!program)return; glClear(GL_COLOR_BUFFER_BIT); glUseProgram(program);
    // Stylized arena floor and concentric markings (placeholder renderer, not extracted game assets).
    rect(-1.f,-.10f,1.f,-1.f,.12f,.18f,.23f);
    circle(0.f,-.32f,.80f,.40f,.22f,.29f,.32f);
    circle(0.f,-.32f,.66f,.32f,.12f,.18f,.21f);
    circle(0.f,-.32f,.50f,.24f,.24f,.31f,.33f);
    circle(0.f,-.32f,.34f,.16f,.12f,.18f,.21f);
    rect(-1.f,.42f,1.f,1.f,.07f,.11f,.16f);
    // Two abstract fighter silhouettes; geometry is intentionally temporary.
    float px=-.34f+moveX*.20f, py=-.12f+moveY*.12f+(isJump?.12f:0.f);
    circle(px,py+.24f,.075f,.085f,.91f,.72f,.53f);
    rect(px-.065f,py+.02f,px+.065f,py+.19f,.12f,.34f,.62f);
    rect(px-.15f,py+.10f,px-.07f,py+.16f,.91f,.72f,.53f);
    rect(px+.07f,py+.10f,px+.15f,py+.16f,.91f,.72f,.53f);
    rect(px-.06f,py-.14f,px-.005f,py+.02f,.10f,.14f,.20f);
    rect(px+.005f,py-.14f,px+.06f,py+.02f,.10f,.14f,.20f);
    circle(.36f,-.02f,.07f,.08f,.80f,.48f,.37f);
    rect(.30f,-.25f,.42f,-.08f,.48f,.17f,.20f);
    if(isGuard){ circle(px,py+.10f,.22f,.30f,.15f,.55f,.86f); }
    if(isAttack){ circle(px+.20f,py+.10f,.13f,.12f,1.f,.55f,.12f); }
    if(isChakra){ circle(px,py+.12f,.27f,.34f,.18f,.45f,.95f); }
    // HUD bars.
    rect(-.92f,.78f,-.18f,.86f,.08f,.08f,.10f); rect(-.91f,.79f,-.36f,.85f,.18f,.72f,.34f);
    rect(.18f,.78f,.92f,.86f,.08f,.08f,.10f); rect(.18f,.79f,.67f,.85f,.80f,.24f,.22f);
}
