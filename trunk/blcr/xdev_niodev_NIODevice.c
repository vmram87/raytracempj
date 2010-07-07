#include "xdev_niodev_NIODevice.h"
#include <stdio.h>
#include <stdlib.h>
#include <fcntl.h>
#include <string.h>

#include "blcr_common.h"
#include "libcr.h"


JNIEnv* jenv;
jobject jobj;


static char context_filename[80];
cr_checkpoint_handle_t crut_cr_handle;
static char*  versionNum;

static jstring stoJstring(JNIEnv* env, const char* pat);
static char* jstringTostring(JNIEnv* env, jstring jstr);

static int
callback(void *arg)
{
    int ret;

	printf("C:In the call back1\n");
	fflush(stdout);

	//jmethodID mid;
	//jclass cls = (*jenv)->GetObjectClass(jenv,jobj);


	//mid = (*jenv)->GetMethodID(jenv,cls,"preProcess","()V");
	//(*jenv)->CallObjectMethod(jenv, jobj, mid);


    printf("C:In the call back2\n");
    fflush(stdout);
    ret = cr_checkpoint(0);
    if (ret > 0) {
        printf("C:restart\n");
        //mid = (*jenv)->GetMethodID(jenv,cls,"processRestart","()V");
       // (*jenv)->CallObjectMethod(jenv, jobj, mid);
    } else if (ret == 0) {
        printf("C:continue\n");
       // mid = (*jenv)->GetMethodID(jenv,cls,"processContinue","()V");
       // (*jenv)->CallObjectMethod(jenv, jobj, mid);
    } else {
        printf("C:errno:%d\n",errno);
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

    sprintf(context_filename,"%s/context.%d", cwd, pid, versionNum);

    return context_filename;
}



JNIEXPORT void JNICALL Java_xdev_niodev_NIODevice_setCallBack(JNIEnv * jEnv, jobject jObj){

	cr_client_id_t client_id = 0;

	client_id = cr_init();

	 if(NULL == jObj)
	 {
	  printf("obj is null\n");
	  return;
	 }


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

	fflush(stdout);
}


JNIEXPORT jint JNICALL Java_xdev_niodev_NIODevice_checkpoint(JNIEnv * jEnv, jobject jObj, jstring versionId){

	jenv=jEnv;
	jobj=jObj;

	versionNum = jstringTostring(jenv, versionId);

	init_context_filename();

	cr_checkpoint_args_t cr_args;

	/* remove existing context file, if any */
	(void)unlink(context_filename);

	/* open the context file */
	printf("opening the context file: %s\n", context_filename);
	int ret = open(context_filename,  O_RDWR | O_CREAT | O_TRUNC);
	if (ret < 0) {
		error("open");
		exit(1);
	}

	cr_initialize_checkpoint_args_t(&cr_args);
	cr_args.cr_fd = ret;
	cr_args.cr_scope = CR_SCOPE_PROC;

	printf("opening file ret: %d\n", ret);
	fflush(stdout);

	/* issue the request */
	ret = cr_request_checkpoint(&cr_args, &crut_cr_handle);
	printf("cr_request_checkpoint cr_args.cr_fd : %d\n", cr_args.cr_fd );
	fflush(stdout);
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
		printf("cr_poll_checkpoint_msg ret: %d\n", ret);
		fflush(stdout);
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



