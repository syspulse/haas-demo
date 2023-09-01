import { Badge } from "primereact/badge";
import { Sidebar } from "primereact/sidebar";
import { useEffect, useState } from "react";
import { Message } from "primereact/message";
import "./styles.scss";
import {
  useGetUnreadNotificationsQuery,
  useListenNotificationsQuery,
  useMarkAsReadMutation,
} from "@app/common/services/nofications.service";
import parseStream from "@app/common/utils/parseStream";
import moment from "moment";
import { useDispatch } from "react-redux";
import { jobApi } from "@app/common/services/job.service";
import { objectToTree } from "@app/common/utils/parseJson";
import { notificationsApi } from "@app/common/services/nofications.service";

export default function AppNotifications() {
  const { data } = useGetUnreadNotificationsQuery();
  const [markAsRead] = useMarkAsReadMutation();
  const socketNotifications = useListenNotificationsQuery();
  const [visible, setVisible] = useState(false);

  const [notifications, setNotifications] = useState<any[]>([]);
  const dispatch = useDispatch();
  const [expanded, setExpanded] = useState<any[]>([]);

  useEffect(() => {
    if (data && data.notifys) {
      setNotifications(data.notifys);
    }

    if (socketNotifications && socketNotifications?.data?.length > 0) {
      dispatch(jobApi.util.invalidateTags(["FetchJobs"]));
      dispatch(notificationsApi.util.invalidateTags(["FetchNotifications"]));

      /**
       * Correct solution, but notification from socket should be fixed in BE no notification id in message
       * 
          const latestNotification = socketNotifications.data[socketNotifications.data.length - 1];
          const parsed: any[] = parseStream(latestNotification);

          setNotifications((prev: any[]) => [...parsed, ...prev]);
       */
    }
  }, [data, socketNotifications]);
  return (
    <div className='appNotificationsRoot'>
      <i className='pi pi-bell p-overlay-badge' onClick={() => setVisible(true)}>
        {notifications.length > 0 && (
          <Badge severity='danger' value={notifications.length > 9 ? "9+" : notifications.length}></Badge>
        )}
      </i>
      {notifications.length > 0 && (
        <Sidebar visible={visible} position='right' showCloseIcon={false} onHide={() => setVisible(false)}>
          <div className='messagesList'>
            {notifications.map((notification: any) => (
              <Message
                key={notification.id}
                severity='info'
                text={
                  <div className='notificationContent'>
                    <div className='header'>
                      <p className='subj'>
                        <strong>{notification.subj}</strong>
                      </p>
                      <p className='time'>{moment(notification.ts).format("DD MMM YYYY HH:mm:ss")}</p>
                    </div>
                    <div className='msg'>
                      {objectToTree(
                        notification.msg,
                        !expanded.includes(notification.id),
                        setExpanded,
                        notification.id
                      )}
                    </div>
                    <p
                      className='action'
                      onClick={() => {
                        markAsRead(notification.id);
                      }}
                    >
                      Acknowledge
                    </p>
                  </div>
                }
              />
            ))}
          </div>
        </Sidebar>
      )}
    </div>
  );
}
