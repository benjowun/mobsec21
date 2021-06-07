using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.Linq;
using System.Net;
using System.Web;
using System.Web.Mvc;
using MoblieSecServerTest;
using MoblieSecServerTest.Models;

namespace MobileSecServer.Controllers
{
    public class DataController : Controller
    {
        private myModel db = new myModel();

        // GET: Data
        public string Index()
        {
            return "Welcome to MobileSec Server!";
        }

        // GET: Data/SendMsgGet
        public ActionResult SendMsgGet(string userPhone, byte [] message)
        {
            Trace.WriteLine("GET Data/SendMsgGet, userPhone: " + userPhone + ", message: " + message);
            if(userPhone != null && message != null)
            {
                int userId = -1;
                List<Message> msgList = db.Messages.ToList();

                foreach (Message msg1 in msgList)
                {
                    if (msg1.userPhone.Equals(userPhone))
                    {
                        if (msg1.userMsgId > userId)
                        {
                            userId = msg1.userMsgId;
                        }
                    }
                }
                userId++;
                Message msg = new Message { userPhone = userPhone, text = message, userMsgId = userId, msgRead = false };

                if (ModelState.IsValid)
                {
                    db.Messages.Add(msg);
                    db.SaveChanges();
                    return new HttpStatusCodeResult(200);
                }
            }

            return new HttpStatusCodeResult(HttpStatusCode.BadRequest);
        }

        // POST Data/SendMsgPost, data need to be in JSON format
        [HttpPost]
        public ActionResult SendMsgPost(string userPhone, byte [] message)
        {
            Trace.WriteLine("GET Data/SendMsgPost, userPhone: " + userPhone + ", message: " + message);
            if (userPhone != null && message != null)
            {
                int userId = -1;
                List<Message> msgList = db.Messages.ToList();

                foreach (Message msg1 in msgList)
                {
                    if (msg1.userPhone.Equals(userPhone))
                    {
                        if (msg1.userMsgId > userId)
                        {
                            userId = msg1.userMsgId;
                        }
                    }
                }
                userId++;
                Message msg = new Message { userPhone = userPhone, text = message, userMsgId = userId, msgRead = false };

                if (ModelState.IsValid)
                {
                    db.Messages.Add(msg);
                    db.SaveChanges();
                    return new HttpStatusCodeResult(200);
                }
            }

            return new HttpStatusCodeResult(HttpStatusCode.BadRequest);
        }

        // POST Data/ClearMsgPost
        [HttpPost]
        public ActionResult ClearMsgPost(string userPhone)
        {
            Trace.WriteLine("GET Data/ClearMsgPost, userPhone: " + userPhone);
            if (userPhone != null)
            {
                List<Message> delList = new List<Message>();
                List<Message> msgList = db.Messages.ToList();

                foreach (Message msg in msgList)
                {
                    if (msg.userPhone.Equals(userPhone))
                    {
                        delList.Add(msg);
                    }
                }

                if (ModelState.IsValid)
                {
                    foreach (Message msg in delList)
                    {
                        db.Messages.Remove(msg);
                        db.SaveChanges();
                    }
                    return new HttpStatusCodeResult(200);
                }
            }

            return new HttpStatusCodeResult(HttpStatusCode.BadRequest);
        }

        // POST Data/GetMsgPost
        [HttpPost]
        public ActionResult GetMsgPost(string userPhone, int userId)
        {
            Trace.WriteLine("GET Data/GetMsgPost, userPhone: " + userPhone + ", userId: " + userId);
            foreach (Message msg in db.Messages.ToList())
            {
                if (msg.userPhone.Equals(userPhone) && msg.userMsgId == userId)
                {
                    string base64String = Convert.ToBase64String(msg.text, 0, msg.text.Length);
                    return Json(base64String);
                }
            }

            return new HttpStatusCodeResult(HttpStatusCode.BadRequest);
        }

        // POST Data/DeleteMsgPost
        [HttpPost]
        public ActionResult DeleteMsgPost(string userPhone, int userId)
        {
            Trace.WriteLine("GET Data/DeleteMsgPost, userPhone: " + userPhone + ", userId: " + userId);
            Message delMsg = null;

            foreach (Message msg in db.Messages.ToList())
            {
                if (msg.userPhone == userPhone && msg.userMsgId == userId)
                {
                    delMsg = msg;
                }
            }

            if(delMsg != null && ModelState.IsValid)
            {
                db.Messages.Remove(delMsg);
                db.SaveChanges();
                return new HttpStatusCodeResult(200);
            }

            return new HttpStatusCodeResult(HttpStatusCode.BadRequest);
        }

        public ActionResult CleanUp(string code)
        {
            if(code.Equals("admin"))
            {
                List<Message> msgs = db.Messages.ToList();
                foreach (Message msg in msgs)
                {
                    db.Messages.Remove(msg);
                    db.SaveChanges();
                }
                return new HttpStatusCodeResult(200);
            }

            return new HttpStatusCodeResult(HttpStatusCode.BadRequest);
        }
    }
}