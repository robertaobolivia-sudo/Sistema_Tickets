const GROUPS = [
    { groupId: 'navDashboardGroup',  submenuId: 'navDashboardSubmenu',  toggleId: 'navDashboardToggle' },
    { groupId: 'navClientesGroup',   submenuId: 'navClientesSubmenu',   toggleId: 'navClientesToggle' },
    { groupId: 'navIndicadoresGroup',submenuId: 'navIndicadoresSubmenu',toggleId: 'navIndicadoresToggle' },
    { groupId: 'navRelatoriosGroup', submenuId: 'navRelatoriosSubmenu', toggleId: 'navRelatoriosToggle' },
];

export function closeOtherSidebarGroups(exceptGroupId) {
    GROUPS
        .filter(g => g.groupId !== exceptGroupId)
        .forEach(({ groupId, submenuId, toggleId }) => {
            const group = document.getElementById(groupId);
            if (!group?.classList.contains('is-open')) return;
            group.classList.remove('is-open');
            document.getElementById(submenuId)?.classList.add('hidden');
            document.getElementById(toggleId)?.setAttribute('aria-expanded', 'false');
        });
}
