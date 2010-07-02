#include "xdev_niodev_NIODevice.h"
#include <stdio.h>
#include <stdlib.h>
#include <fcntl.h>
#include <string.h>

#include "blcr_common.h"
#include "libcr.h"


JNIEXPORT void JNICALL Java_xdev_niodev_NIODevice_setCallBack(JNIEnv * jEnv, jobject jObj, jstring versionId){

}


JNIEXPORT jint JNICALL Java_xdev_niodev_NIODevice_checkpoint(JNIEnv * jEnv, jobject jObj){
	return 1;
}
