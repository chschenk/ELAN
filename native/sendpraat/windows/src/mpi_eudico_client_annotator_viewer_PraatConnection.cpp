#include "mpi_eudico_client_annotator_viewer_PraatConnection.h"
#include "sendpraat.h"

#ifdef __cplusplus
extern "C" {
#endif
/*
 * Class:     mpi_eudico_client_annotator_viewer_PraatConnection
 * Method:    sendpraatNative
 * Signature: (Ljava/lang/String;JLjava/lang/String;)Ljava/lang/String;
 * Return: NULL in case of success, an error message otherwise
 */
JNIEXPORT jstring JNICALL Java_mpi_eudico_client_annotator_viewer_PraatConnection_sendpraatNative
    (JNIEnv *env, jclass callerObj, jstring program, jlong timeOut, jstring command) {
        jboolean isCopyP;
        jboolean isCopyC;
        const char * programC;
        const char * commandC;
        
        programC = env->GetStringUTFChars(program, &isCopyP);
        
        commandC = env->GetStringUTFChars(command, &isCopyC);
        /* provide a buffer for the sendpraat function to store an error message */
        char errorMessage [1000];
        errorMessage[0] = '\0';
        /* call sendpraat */
        sendpraat(programC, commandC, errorMessage);
        
        if (isCopyP == JNI_TRUE) {// or release anyway?
            env->ReleaseStringUTFChars(program, programC);
        }
        
        if (isCopyC == JNI_TRUE) {
            env->ReleaseStringUTFChars(command, commandC);
        }
        
        if(errorMessage[0] == '\0') {
            //printf("Native no error.\n");// comment out
            fflush(stdout);
            return NULL;
        }

        jstring errorString = env->NewStringUTF(errorMessage);
        
        fflush(stdout);
        return errorString;
    }

#ifdef __cplusplus
}
#endif

