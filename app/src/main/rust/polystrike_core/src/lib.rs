use jni::JNIEnv;
use jni::objects::JClass;
use jni::sys::jstring;

#[no_mangle]
pub extern "C" fn Java_com_example_nativebridge_NativeEngineBridge_stringFromRust(
    env: JNIEnv,
    _class: JClass,
) -> jstring {
    let output = env
        .new_string("Rust Core Engine 1.0 (Direct Native / Memory-Safe / Concurrency Ready)")
        .expect("Failed to create Java string from Rust");
    output.into_raw()
}
