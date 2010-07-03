/*
 * TestSo.c
 *
 *  Created on: Jun 30, 2010
 *      Author: root
 */
#include "TestSo.h"
#include <stdio.h>
#include <stdlib.h>
#include <fcntl.h>
#include <string.h>



#include "blcr_common.h"
#include "libcr.h"

typedef struct CALLBACKARGS {
	JNIEnv* jenv;
	jclass jobj;
} CALLBACKARGS_T;

JNIEnv* jenv;
jclass jobj;


enum crut_state {
    crut_error=0,
    crut_continue,
    crut_restart,
};

static int crut_checkpoint_status  = 0;
static int crut_saved_error = 0;

static char context_filename[80];
cr_checkpoint_handle_t crut_cr_handle;

static int
callback(void *arg)
{
    int ret;

    //CALLBACKARGS_T cbArgs = *((CALLBACKARGS_T*) arg);
   // JNIEnv* jEnv=cbArgs.env;
    //jclass cls=cbArgs.cls;
    jmethodID mid;
	jclass cls = (*jenv)->GetObjectClass(jenv,jobj);

	mid = (*jenv)->GetMethodID(jenv,cls,"preProcess","()V");
	(*jenv)->CallObjectMethod(jenv, jobj, mid);



    printf("C:In the call back\n");
    ret = cr_checkpoint(0);
    if (ret > 0) {
        crut_checkpoint_status = crut_restart;
        printf("C:restart\n");
        mid = (*jenv)->GetMethodID(jenv,cls,"processRestart","()V");
        (*jenv)->CallObjectMethod(jenv, jobj, mid);
    } else if (ret == 0) {
        crut_checkpoint_status = crut_continue;
        printf("C:continue\n");
        mid = (*jenv)->GetMethodID(jenv,cls,"processContinue","()V");
        (*jenv)->CallObjectMethod(jenv, jobj, mid);
    } else {
        crut_checkpoint_status = crut_error;
        crut_saved_error = ret;
        printf("C:error\n");
    }

    return 0;
}

static char *
init_context_filename(void)
{
	int pid;
	char cwd[100];
	char *p;

    pid = getpid();
    if (pid < 0) {
        error("getpid");
	return NULL;
    }

    p = cwd;
    p = getcwd(cwd, sizeof(cwd));
    if (p == NULL || p != cwd) {
		error("getcwd");
        return NULL;
    }

    sprintf(context_filename,"%s/context.%d", cwd, pid);

    return context_filename;
}

/*
 * Class:     TestSo
 * Method:    setCallBack
 * Signature: (Ljava/lang/String;)V
 */
JNIEXPORT void JNICALL Java_TestSo_setCallBack(JNIEnv * jEnv, jobject jObj){

	cr_client_id_t client_id = 0;

	client_id = cr_init();
	CALLBACKARGS_T cbArgs;

	jenv=jEnv;
	jobj=jObj;

	jclass cls = (*jEnv)->GetObjectClass(jEnv,jObj);
	 if(NULL == jObj)
	 {
	  printf("obj is null\n");
	  return;
	 }

	//fflush(stdout);


	if (client_id < 0) {
		printf("cr_init() failed.  ret=%d.  errno=%d\n", client_id, errno);
		exit(1);
	}

	printf("cr_register_callback()\n");

	int ret = cr_register_callback(callback, NULL, CR_SIGNAL_CONTEXT);
	if (ret < 0) {
		printf("cr_register_callback() failed.  ret=%d\n", ret);
		exit(1);
	}
}

/*
 * Class:     TestSo
 * Method:    checkpoint
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_TestSo_checkpoint(JNIEnv * jEnv, jobject jObj){

	jmethodID mid;
	jclass cls = (*jenv)->GetObjectClass(jenv,jobj);

	init_context_filename();

	cr_checkpoint_args_t cr_args;

	/* remove existing context file, if any */
	(void)unlink(context_filename);

	/* open the context file */
	printf("opening the context file: %s\n", context_filename);
	int ret = open(context_filename, O_WRONLY | O_CREAT | O_TRUNC , 0600);
	if (ret < 0) {
		error("open");
		exit(1);
	}

	cr_initialize_checkpoint_args_t(&cr_args);
	cr_args.cr_fd = ret;
	cr_args.cr_scope = CR_SCOPE_PROC;

	/* issue the request */
	ret = cr_request_checkpoint(&cr_args, &crut_cr_handle);
	if (ret < 0) {
		(void)close(cr_args.cr_fd);
		(void)unlink(context_filename);
		error("cr_request_checkpoint");
		exit(1);
	}

	/* wait for the request to complete */
	do {
	char *kmsgs = NULL;
	ret = cr_poll_checkpoint_msg(&crut_cr_handle, NULL, &kmsgs);
	if (ret < 0) {
		if ((ret == CR_POLL_CHKPT_ERR_POST) && (errno == CR_ERESTARTED)) {
		/* restarting -- not an error */
				ret = 0;
		} else if (errno == EINTR) {
				/* retry */
				;
			} else {
		int saved_errno = errno;
		fprintf(stderr, "cr_poll_checkpoint returned %d: %s\n", ret, cr_strerror(errno));
		if (kmsgs) {
			fputs(kmsgs, stderr);
		}
		errno = saved_errno;
		exit(1);
		}
	} else if (ret == 0) {
			fprintf(stderr, "cr_poll_checkpoint returned unexpected 0\n");
		exit(1);
	}
	} while (ret < 0);


	close(cr_args.cr_fd);

	return ret;


}

static jstring stoJstring(JNIEnv* env, const char* pat)
{
       jclass strClass = (*env)->FindClass(env, "Ljava/lang/String;");
       jmethodID ctorID = (*env)->GetMethodID(env, strClass, "<init>", "([BLjava/lang/String;)V");
       jbyteArray bytes = (*env)->NewByteArray(env, strlen(pat));
       (*env)->SetByteArrayRegion(env, bytes, 0, strlen(pat), (jbyte*)pat);
       jstring encoding = (*env)->NewStringUTF(env, "utf-8");
       return (jstring)(*env)->NewObject(env, strClass, ctorID, bytes, encoding);
}

static char* jstringTostring(JNIEnv* env, jstring jstr)
{
       char* rtn = NULL;
       jclass clsstring = (*env)->FindClass(env, "java/lang/String");
       jstring strencode = (*env)->NewStringUTF(env, "utf-8");
       jmethodID mid = (*env)->GetMethodID(env, clsstring, "getBytes", "(Ljava/lang/String;)[B");
       jbyteArray barr= (jbyteArray)(*env)->CallObjectMethod(env, jstr, mid, strencode);
       jsize alen = (*env)->GetArrayLength(env, barr);
       jbyte* ba = (*env)->GetByteArrayElements(env, barr, JNI_FALSE);
       if (alen > 0)
       {
                 rtn = (char*)malloc(alen + 1);
                 memcpy(rtn, ba, alen);
                 rtn[alen] = 0;
       }
       (*env)->ReleaseByteArrayElements(env, barr, ba, 0);
       return rtn;
}

