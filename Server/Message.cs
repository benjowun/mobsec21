using System;
using System.Collections.Generic;
using System.ComponentModel.DataAnnotations;
using System.Linq;
using System.Web;

namespace MoblieSecServerTest.Models
{
    public class Message
    {
        [Key]
        public int msgId { get; set; }
        public int userMsgId { get; set; }
        public bool msgRead { get; set; }
        public byte [] text { get; set; }
        public string userPhone { get; set; }
    }
}