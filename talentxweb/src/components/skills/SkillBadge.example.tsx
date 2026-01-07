import React from 'react';
import SkillBadge from './SkillBadge';
import { ProficiencyLevel } from '../../types';

/**
 * Example usage of the SkillBadge component
 * This file demonstrates various ways to use the SkillBadge component
 */
const SkillBadgeExample: React.FC = () => {
  const handleSkillClick = (skillName: string) => {
    console.log(`Clicked on skill: ${skillName}`);
  };

  return (
    <div style={{ padding: '20px', display: 'flex', flexDirection: 'column', gap: '20px' }}>
      <h2>SkillBadge Component Examples</h2>

      {/* Basic Usage */}
      <section>
        <h3>Basic Usage</h3>
        <div style={{ display: 'flex', gap: '8px', flexWrap: 'wrap' }}>
          <SkillBadge
            skillName="JavaScript"
            proficiencyLevel={ProficiencyLevel.INTERMEDIATE}
          />
          <SkillBadge
            skillName="React"
            proficiencyLevel={ProficiencyLevel.ADVANCED}
          />
          <SkillBadge
            skillName="TypeScript"
            proficiencyLevel={ProficiencyLevel.EXPERT}
          />
          <SkillBadge
            skillName="CSS"
            proficiencyLevel={ProficiencyLevel.BEGINNER}
          />
        </div>
      </section>

      {/* With Verification */}
      <section>
        <h3>With Verification Status</h3>
        <div style={{ display: 'flex', gap: '8px', flexWrap: 'wrap' }}>
          <SkillBadge
            skillName="Java"
            proficiencyLevel={ProficiencyLevel.ADVANCED}
            isVerified={true}
            verifiedAt="2023-12-01T10:00:00Z"
          />
          <SkillBadge
            skillName="Python"
            proficiencyLevel={ProficiencyLevel.INTERMEDIATE}
            isVerified={false}
          />
        </div>
      </section>

      {/* Different Sizes */}
      <section>
        <h3>Different Sizes</h3>
        <div style={{ display: 'flex', gap: '8px', alignItems: 'center', flexWrap: 'wrap' }}>
          <SkillBadge
            skillName="Node.js"
            proficiencyLevel={ProficiencyLevel.ADVANCED}
            size="small"
            isVerified={true}
          />
          <SkillBadge
            skillName="Express"
            proficiencyLevel={ProficiencyLevel.INTERMEDIATE}
            size="medium"
            isVerified={true}
          />
          <SkillBadge
            skillName="MongoDB"
            proficiencyLevel={ProficiencyLevel.EXPERT}
            size="large"
            isVerified={true}
          />
        </div>
      </section>

      {/* With Additional Details */}
      <section>
        <h3>With Additional Details</h3>
        <div style={{ display: 'flex', gap: '8px', flexWrap: 'wrap' }}>
          <SkillBadge
            skillName="AWS"
            proficiencyLevel={ProficiencyLevel.ADVANCED}
            category="Cloud Computing"
            yearsOfExperience={3}
            lastUsedYear={2023}
            isVerified={true}
            verifiedAt="2023-11-15T14:30:00Z"
          />
          <SkillBadge
            skillName="Docker"
            proficiencyLevel={ProficiencyLevel.INTERMEDIATE}
            category="DevOps"
            yearsOfExperience={2}
            lastUsedYear={2023}
            isVerified={false}
          />
        </div>
      </section>

      {/* Clickable Skills */}
      <section>
        <h3>Clickable Skills</h3>
        <div style={{ display: 'flex', gap: '8px', flexWrap: 'wrap' }}>
          <SkillBadge
            skillName="GraphQL"
            proficiencyLevel={ProficiencyLevel.INTERMEDIATE}
            onClick={() => handleSkillClick('GraphQL')}
            isVerified={true}
          />
          <SkillBadge
            skillName="Redux"
            proficiencyLevel={ProficiencyLevel.ADVANCED}
            onClick={() => handleSkillClick('Redux')}
            isVerified={false}
          />
        </div>
        <p style={{ fontSize: '12px', color: '#6c757d', marginTop: '8px' }}>
          Click on the skills above to see console output
        </p>
      </section>

      {/* All Proficiency Levels */}
      <section>
        <h3>All Proficiency Levels</h3>
        <div style={{ display: 'flex', gap: '8px', flexWrap: 'wrap' }}>
          <SkillBadge
            skillName="HTML"
            proficiencyLevel={ProficiencyLevel.BEGINNER}
          />
          <SkillBadge
            skillName="CSS"
            proficiencyLevel={ProficiencyLevel.INTERMEDIATE}
          />
          <SkillBadge
            skillName="JavaScript"
            proficiencyLevel={ProficiencyLevel.ADVANCED}
          />
          <SkillBadge
            skillName="React"
            proficiencyLevel={ProficiencyLevel.EXPERT}
          />
        </div>
      </section>

      {/* Skills Grid Example */}
      <section>
        <h3>Skills Grid Layout</h3>
        <div style={{
          display: 'grid',
          gridTemplateColumns: 'repeat(auto-fill, minmax(120px, 1fr))',
          gap: '8px',
          padding: '16px',
          backgroundColor: '#f8f9fa',
          borderRadius: '8px'
        }}>
          {[
            { name: 'React', level: ProficiencyLevel.EXPERT, verified: true },
            { name: 'Vue.js', level: ProficiencyLevel.INTERMEDIATE, verified: false },
            { name: 'Angular', level: ProficiencyLevel.BEGINNER, verified: false },
            { name: 'Node.js', level: ProficiencyLevel.ADVANCED, verified: true },
            { name: 'Python', level: ProficiencyLevel.INTERMEDIATE, verified: true },
            { name: 'Java', level: ProficiencyLevel.ADVANCED, verified: true },
            { name: 'SQL', level: ProficiencyLevel.EXPERT, verified: true },
            { name: 'Git', level: ProficiencyLevel.ADVANCED, verified: false }
          ].map((skill, index) => (
            <SkillBadge
              key={index}
              skillName={skill.name}
              proficiencyLevel={skill.level}
              isVerified={skill.verified}
              size="small"
            />
          ))}
        </div>
      </section>
    </div>
  );
};

export default SkillBadgeExample;