//
//  SMIParticipantRole.h
//  SMIClientCore
//
//  Created by Nigel Brown on 2022-09-16.
//

#import <Foundation/Foundation.h>

/// Participant roles.
/// Possible values:
/// * `SMIParticipantRoleSystem`
/// * `SMIParticipantRoleUser`
/// * `SMIParticipantRoleAgent`
/// * `SMIParticipantRoleChatbot`
/// * `SMIParticipantRoleRouter`
/// * `SMIParticipantRoleSupervisor`
typedef NSString* SMIParticipantRole NS_TYPED_ENUM
NS_SWIFT_NAME(ParticipantRole);

/// Participant system role.
FOUNDATION_EXPORT SMIParticipantRole SMIParticipantRoleSystem;

/// Participant user role.
FOUNDATION_EXPORT SMIParticipantRole SMIParticipantRoleUser;

/// Participant agent role.
FOUNDATION_EXPORT SMIParticipantRole SMIParticipantRoleAgent;

/// Participant chatbot role.
FOUNDATION_EXPORT SMIParticipantRole SMIParticipantRoleChatbot;

/// Participant router role.
FOUNDATION_EXPORT SMIParticipantRole SMIParticipantRoleRouter;

/// Participant supervisor role.
FOUNDATION_EXPORT SMIParticipantRole SMIParticipantRoleSupervisor;
