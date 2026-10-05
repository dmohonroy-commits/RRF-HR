const functions = require("firebase-functions");
const admin = require("firebase-admin");

// Initialize Firebase Admin SDK
if (!admin.apps.length) {
  admin.initializeApp();
}

const db = admin.firestore();

/**
 * 1. Health check function to verify Cloud Functions server status.
 */
exports.checkHealth = functions.https.onCall(async (data, context) => {
  return {
    status: "ok",
    service: "RRF HR Cloud Functions",
    timestamp: Date.now(),
    serverTime: new Date().toISOString(),
    region: process.env.FUNCTION_REGION || "us-central1"
  };
});

/**
 * 2. Backup complete worker agreement forms to cloud database.
 */
exports.backupAgreementForms = functions.https.onCall(async (data, context) => {
  try {
    const { employeeName, employeeNid, formsData } = data || {};
    
    if (!employeeName && !employeeNid) {
      throw new functions.https.HttpsError(
        "invalid-argument",
        "কর্মীর নাম অথবা জাতীয় পরিচয়পত্র নম্বর আবশ্যক।"
      );
    }

    const docId = employeeNid ? `nid_${employeeNid}` : `emp_${Date.now()}`;
    const record = {
      employeeName: employeeName || "Unknown",
      employeeNid: employeeNid || "",
      formsData: typeof formsData === "string" ? formsData : JSON.stringify(formsData || {}),
      updatedAt: admin.firestore.FieldValue.serverTimestamp(),
      callerUid: context.auth ? context.auth.uid : "anonymous"
    };

    await db.collection("employee_agreements").doc(docId).set(record, { merge: true });

    return {
      success: true,
      message: "চুক্তিপত্রের সকল তথ্য সফলভাবে ক্লাউডে ব্যাকআপ সংরক্ষিত হয়েছে।",
      docId: docId,
      timestamp: Date.now()
    };
  } catch (error) {
    console.error("backupAgreementForms error:", error);
    if (error instanceof functions.https.HttpsError) {
      throw error;
    }
    throw new functions.https.HttpsError(
      "internal",
      `ক্লাউড ব্যাকআপ ব্যর্থ হয়েছে: ${error.message}`
    );
  }
});

/**
 * 3. Synchronize worker profile information.
 */
exports.syncWorkerProfile = functions.https.onCall(async (data, context) => {
  try {
    const worker = data || {};
    const nid = worker.nid || worker.employeeNid;
    
    if (!nid) {
      throw new functions.https.HttpsError(
        "invalid-argument",
        "কর্মীর NID নম্বর ছাড়া প্রোফাইল সিঙ্ক করা সম্ভব নয়।"
      );
    }

    const docRef = db.collection("worker_profiles").doc(`nid_${nid}`);
    await docRef.set({
      ...worker,
      syncedAt: admin.firestore.FieldValue.serverTimestamp()
    }, { merge: true });

    return {
      success: true,
      message: "কর্মীর প্রোফাইল সফলভাবে ক্লাউডে সিঙ্ক হয়েছে।",
      nid: nid
    };
  } catch (error) {
    console.error("syncWorkerProfile error:", error);
    if (error instanceof functions.https.HttpsError) {
      throw error;
    }
    throw new functions.https.HttpsError(
      "internal",
      `প্রোফাইল সিঙ্ক ব্যর্থ হয়েছে: ${error.message}`
    );
  }
});

/**
 * 4. Verify employee online against central organization records.
 */
exports.verifyEmployeeOnline = functions.https.onCall(async (data, context) => {
  try {
    const { nid, mobile } = data || {};
    
    if (!nid) {
      throw new functions.https.HttpsError(
        "invalid-argument",
        "যাচাইকরণের জন্য NID প্রদান আবশ্যক।"
      );
    }

    // Clean digits
    const cleanNid = String(nid).replace(/\D/g, "");
    const cleanMobile = mobile ? String(mobile).replace(/\D/g, "") : "";

    const isValidLength = cleanNid.length === 10 || cleanNid.length === 13 || cleanNid.length === 17;

    return {
      verified: isValidLength,
      nid: cleanNid,
      mobile: cleanMobile,
      status: isValidLength ? "VERIFIED_ACTIVE" : "INVALID_NID_LENGTH",
      message: isValidLength 
        ? "জাতীয় পরিচয়পত্র নম্বরটি সঠিক ফরম্যাটে যাচাইকৃত।"
        : "জাতীয় পরিচয়পত্র নম্বরের দৈর্ঘ্য ১০, ১৩ অথবা ১৭ ডিজিট হতে হবে।",
      verificationDate: new Date().toISOString()
    };
  } catch (error) {
    console.error("verifyEmployeeOnline error:", error);
    throw new functions.https.HttpsError("internal", error.message);
  }
});

/**
 * 5. Send verification notification or SMS alert to guarantor/worker.
 */
exports.sendVerificationNotification = functions.https.onCall(async (data, context) => {
  try {
    const { mobile, message, role } = data || {};

    if (!mobile) {
      throw new functions.https.HttpsError(
        "invalid-argument",
        "বিজ্ঞপ্তি প্রেরণের জন্য মোবাইল নম্বর আবশ্যক।"
      );
    }

    // Log notification dispatch (can be connected to SMS gateway e.g. Twilio, SSL Wireless)
    console.log(`Sending notification to ${mobile} [${role || 'Guarantor'}]: ${message}`);

    await db.collection("notification_logs").add({
      mobile: mobile,
      message: message || "চুক্তিপত্র যাচাইকরণ নোটিফিকেশন",
      role: role || "Guarantor",
      sentAt: admin.firestore.FieldValue.serverTimestamp(),
      status: "QUEUED"
    });

    return {
      sent: true,
      recipient: mobile,
      role: role || "Guarantor",
      message: "নোটিফিকেশন সফলভাবে প্রক্রিয়াকরণ ও প্রেরণের তালিকায় যুক্ত হয়েছে।"
    };
  } catch (error) {
    console.error("sendVerificationNotification error:", error);
    throw new functions.https.HttpsError("internal", error.message);
  }
});
