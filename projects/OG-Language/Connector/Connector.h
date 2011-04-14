/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#ifndef __inc_og_language_connector_connector_h
#define __inc_og_language_connector_connector_h

// Public definitions for the main interface to the library

#include <assert.h>
#include <Util/Asynchronous.h>
#include <Util/Fudge.h>
#include "Client.h"
#include "SynchronousCalls.h"

class CConnector : public CClientService::CStateChange, public CClientService::CMessageReceived {
public:
	class CCallback {
	private:
		friend class CConnector;
		CAtomicInt m_oRefCount;
	protected:
		virtual void OnMessage (FudgeMsg msgPayload) = 0;
		virtual void OnThreadDisconnect () { }
	public:
		CCallback () : m_oRefCount (1) { }
		virtual ~CCallback () { assert (!m_oRefCount.Get ()); }
		void Retain () { m_oRefCount.IncrementAndGet (); }
		static void Release (CCallback *poCallback) { if (!poCallback->m_oRefCount.DecrementAndGet ()) delete poCallback; }
	};
private:
	CAtomicInt m_oRefCount;
	CClientService *m_poClient;
	CMutex m_oControlMutex;
	CAtomicPointer<CSemaphore*> m_oStartupSemaphorePtr;
	class CCallbackEntry {
	private:
		CAtomicInt m_oRefCount;
		FudgeString m_strClass;
		CCallback *m_poCallback;
	public:
		CCallbackEntry *m_poNext;
		CCallbackEntry (FudgeString strClass, CCallback *poCallback, CCallbackEntry *poNext)
			: m_oRefCount (1) {
			m_strClass = strClass;
			poCallback->Retain ();
			m_poCallback = poCallback;
			m_poNext = poNext;
		}
		~CCallbackEntry () {
			assert (m_oRefCount.Get () == 0);
			if (m_strClass) FudgeString_release (m_strClass);
			CCallback::Release (m_poCallback);
		}
		void Retain () { m_oRefCount.IncrementAndGet (); }
		static void Release (CCallbackEntry *poEntry) { if (!poEntry->m_oRefCount.DecrementAndGet ()) delete poEntry; }
		bool IsClass (FudgeString strClass) { return !FudgeString_compare (m_strClass, strClass); }
		bool IsCallback (CCallback *poCallback) { return m_poCallback == poCallback; }
		void FreeString () { FudgeString_release (m_strClass); m_strClass = NULL; }
		void OnMessage (FudgeMsg msgPayload);
		void OnThreadDisconnect () { m_poCallback->OnThreadDisconnect (); }
	};
	class CCallbackEntry *m_poCallbacks;
	CSynchronousCalls m_oSynchronousCalls;
	CAsynchronous *m_poDispatch;
	CConnector (CClientService *poClient);
	friend class CConnectorMessageDispatch;
	friend class CConnectorThreadDisconnectDispatch;
	friend class CConnectorDispatcher;
protected:
	void OnStateChange (ClientServiceState ePreviousState, ClientServiceState eNewState);
	void OnMessageReceived (FudgeMsg msg);
	void OnDispatchThreadDisconnect ();
public:
	class CCall {
	private:
		friend class CConnector;
		CSynchronousCallSlot *m_poSlot;
		CCall (CSynchronousCallSlot *poSlot);
	public:
		~CCall ();
		bool Cancel ();
		bool WaitForResult (FudgeMsg *pmsgResponse, unsigned long lTimeout);
	};
	~CConnector ();
	static CConnector *Start (const TCHAR *pszLanguageID);
	bool Stop ();
	bool WaitForStartup (unsigned long lTimeout);
	void Retain () { m_oRefCount.IncrementAndGet (); }
	static void Release (CConnector *poConnector) { if (!poConnector->m_oRefCount.DecrementAndGet ()) delete poConnector; }
	bool Call (FudgeMsg msgPayload, FudgeMsg *pmsgResponse, unsigned long lTimeout);
	CCall *Call (FudgeMsg msgPayload);
	bool Send (FudgeMsg msgPayload);
	bool AddCallback (const TCHAR *pszClass, CCallback *poCallback);
	bool RemoveCallback (CCallback *poCallback);
	bool RecycleDispatchThread ();
};

#endif /* ifndef __inc_og_language_connector_connector_h */
