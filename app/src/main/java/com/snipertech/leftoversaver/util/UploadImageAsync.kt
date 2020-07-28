package com.snipertech.leftoversaver.util

import android.app.Activity
import android.os.AsyncTask
import android.util.Log
import android.widget.Toast
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility
import com.amazonaws.regions.Region
import com.amazonaws.regions.Regions
import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.services.s3.model.Bucket
import com.amazonaws.services.s3.model.CannedAccessControlList
import com.snipertech.leftoversaver.R
import com.snipertech.leftoversaver.util.Constants.bucketName
import java.io.File
import java.lang.ref.WeakReference


class UploadImageAsync(myActivity: Activity) : AsyncTask<String?, String?, String>() {

    private var weakActivity = WeakReference(myActivity)

    override fun doInBackground(vararg p0: String?): String {
        val fileName = p0[0]
        val filePath = p0[1]
        val s3Client = AmazonS3Client(BasicAWSCredentials("accessKey", "secretKey"))
        java.security.Security.setProperty("networkaddress.cache.ttl", "60");
        s3Client.setRegion(Region.getRegion(Regions.US_WEST_1))
        s3Client.endpoint = "https://s3-us-west-1.amazonaws.com/"

        val buckets: List<Bucket> = s3Client.listBuckets()
        for (bucket in buckets) {
            Log.e(
                "Bucket ",
                "Name " + bucket.name.toString() + " Owner " + bucket.getOwner()
                    .toString() + " Date " + bucket.creationDate
            )
        }
        Log.e("Size ", "" + s3Client.listBuckets().size)

        val transferUtility: TransferUtility = TransferUtility.builder()
            .context(weakActivity.get())
            .s3Client(s3Client)
            .build()

        val observer: TransferObserver = transferUtility.upload(
            bucketName,  //this is the bucket name on S3
            fileName,
            File(filePath),
            CannedAccessControlList.PublicRead //to make the file public
        )

        observer.setTransferListener(object : TransferListener {
            override fun onStateChanged(id: Int, state: TransferState) {
                if (state == TransferState.FAILED) {
                    Toast.makeText(
                        weakActivity.get(),
                        weakActivity.get()?.resources?.getString(R.string.error), Toast.LENGTH_LONG
                    ).show()
                }
            }

            override fun onProgressChanged(
                id: Int,
                bytesCurrent: Long,
                bytesTotal: Long
            ) {
            }

            override fun onError(id: Int, ex: Exception?) {
                if (ex != null) {
                    Log.e("Bucket", ex.message)
                }
            }
        })
        return ""
    }

    override fun onPostExecute(result: String?) {
        super.onPostExecute(result)
        // Re-acquire a strong reference to the activity, and verify
        // that it still exists and is active.
        val activity: Activity? = weakActivity.get()
        if (activity == null || activity.isFinishing
            || activity.isDestroyed
        ) {
            // activity is no longer valid, don't do anything!
            return
        }
    }

}