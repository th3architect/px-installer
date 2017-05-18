apiVersion: extensions/v1beta1
kind: DaemonSet
metadata:
  name: portworx
  namespace: kube-system
spec:
  template:
    metadata:
      labels:
        name: portworx
    spec:
      hostNetwork: true
      hostPID: true
      containers:
        - name: portworx
          image: portworx/px-enterprise:latest
          imagePullPolicy: Always
          args:
             ["{{if .Etcd}}-k {{.Etcd}}{{end}}",
              "{{if .Cluster}}-c {{.Cluster}}{{end}}",
              "{{if .DIface}}-d {{.DIface}}{{end}}",
              "{{if .MIface}}-m {{.MIface}}{{end}}",
              "{{if .Drives}}{{.Drives}}{{end}}",
              "{{if .EtcdPasswd}}-userpwd {{.EtcdPasswd}}{{end}}",
              "{{if .EtcdCa}}-ca {{.EtcdCa}}{{end}}",
              "{{if .EtcdCert}}-cert {{.EtcdCert}}{{end}}",
              "{{if .EtcdKey}}-key {{.EtcdKey}}{{end}}",
              "{{if .Acltoken}}-acltoken {{.Acltoken}}{{end}}",
              "{{if .Token}}-t {{.Token}}{{end}}",
              "{{if .Env}}{{.Env}}{{end}}",
              "-x", "kubernetes"]
          livenessProbe:
            initialDelaySeconds: 840 # allow image pull in slow networks
            httpGet:
              host: 127.0.0.1
              path: /status
              port: 9001
          securityContext:
            privileged: true
          volumeMounts:
            - name: dockersock
              mountPath: /var/run/docker.sock
            - name: libosd
              mountPath: /var/lib/osd:shared
            - name: dev
              mountPath: /dev
            - name: etcpwx
              mountPath: /etc/pwx/
            - name: optpwx
              mountPath: /export_bin:shared
            - name: cores
              mountPath: /var/cores
            - name: kubelet
              mountPath: /var/lib/kubelet:shared
            - name: src
              mountPath: /usr/src
            - name: dockerplugins
              mountPath: /run/docker/plugins
      initContainers:
        - name: px-init
          image: harshpx/monitor
          securityContext:
            privileged: true
          volumeMounts:
            - name: hostproc
              mountPath: /media/host/proc
      restartPolicy: Always
      tolerations:
      - key: node-role.kubernetes.io/master
        effect: NoSchedule
      volumes:
        - name: libosd
          hostPath:
            path: /var/lib/osd
        - name: dev
          hostPath:
            path: /dev
        - name: etcpwx
          hostPath:
            path: /etc/pwx
        - name: optpwx
          hostPath:
            path: /opt/pwx/bin
        - name: cores
          hostPath:
            path: /var/cores
        - name: kubelet
          hostPath:
            path: /var/lib/kubelet
        - name: src
          hostPath:
            path: /usr/src
        - name: dockerplugins
          hostPath:
            path: /run/docker/plugins
        - name: dockersock
          hostPath:
            path: /var/run/docker.sock
        - name: hostproc
          hostPath:
            path: /proc
